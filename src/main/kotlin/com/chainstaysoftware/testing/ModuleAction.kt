package com.chainstaysoftware.testing

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.FileIndexFacade
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex


/**
 * AnAction to convert a single Module.
 */
class ModuleAction : AnAction() {
   override fun actionPerformed(event: AnActionEvent) {
      val psiFile = event.getData(PlatformDataKeys.PSI_FILE) ?: return
      val project = psiFile.project
      val module = FileIndexFacade.getInstance(project).getModuleForFile(psiFile.virtualFile) ?: return
      val globalSearchScope = GlobalSearchScope.moduleScope(module)
      val numFiles = numFilesToProcess(project, globalSearchScope)

      ProgressManager.getInstance().runProcessWithProgressSynchronously({
         val fileHandler = FileHandler()
         val progressIndicator = ProgressManager.getInstance().progressIndicator
         var numProcessed = 0.0

            Util.traverseTestFiles(project, { psiFile ->
                  if (progressIndicator.isCanceled)
                     throw ProcessCanceledException()

                  progressIndicator.text = "Processing - ${psiFile.name}"
                  WriteCommandAction.runWriteCommandAction(project) {
                     fileHandler.handle(psiFile)
                  }
                  numProcessed += 1
                  progressIndicator.fraction = Math.max(numProcessed / numFiles, 0.1)
               },
               globalSearchScope)

         progressIndicator.stop()

      }, "Migrate Assertions to AssertJ", true, project)
   }

   private fun numFilesToProcess(project: Project,
                                 globalSearchScope: GlobalSearchScope): Int {
      val instance = ProjectFileIndex.SERVICE.getInstance(project)
      return FileBasedIndex.getInstance().getContainingFiles(
            FileTypeIndex.NAME,
            JavaFileType.INSTANCE,
            globalSearchScope)
         .filter { virtualFile -> instance.isInTestSourceContent(virtualFile) }
         .size
   }
}
