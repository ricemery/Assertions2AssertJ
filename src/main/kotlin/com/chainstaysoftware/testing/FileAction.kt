package com.chainstaysoftware.testing

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction


/**
 * AnAction to convert a single file.
 */
class FileAction : AnAction() {
   override fun actionPerformed(event: AnActionEvent) {
      val psiFile = event.getData(PlatformDataKeys.PSI_FILE) ?: return
      WriteCommandAction.runWriteCommandAction(psiFile.project) {
         FileHandler().handle(psiFile)
      }
   }
}
