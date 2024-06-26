package com.chainstaysoftware.testing

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex


/**
 * AnAction to convert an entire Project.
 */
class ProjectAction : AnAction() {
   override fun actionPerformed(event: AnActionEvent) {
      val project = event.getData(PlatformDataKeys.PROJECT) ?: return
      val numFiles = numFilesToProcess(project)

      if (numFiles == 0) {
         ErrorBalloon().show(project, "No file found to process")
         return
      }

      if (!Util.inClasspath(project, "org.assertj.core.api.Assertions")) {
         ErrorBalloon().show(project, "AssertJ MUST be in the classpath")
         return
      }

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
            })

         progressIndicator.stop()

      }, "Migrate Assertions to AssertJ", true, project)
   }

   private fun numFilesToProcess(project: Project): Int {
      val instance = ProjectFileIndex.getInstance(project)
      return FileTypeIndex.getFiles(JavaFileType.INSTANCE,
            GlobalSearchScope.projectScope(project))
         .filter { virtualFile -> instance.isInTestSourceContent(virtualFile) }
         .size
   }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }
}
