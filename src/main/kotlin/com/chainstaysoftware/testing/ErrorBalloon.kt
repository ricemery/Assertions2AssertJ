package com.chainstaysoftware.testing

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.awt.RelativePoint

class ErrorBalloon {
   fun show(project: Project,
            htmlText: String) {
      val statusBar = WindowManager.getInstance()
         .getStatusBar(project)

      JBPopupFactory.getInstance()
         .createHtmlTextBalloonBuilder(htmlText, MessageType.ERROR, null)
         .setFadeoutTime(7500)
         .createBalloon()
         .show(statusBar.component?.let { RelativePoint.getCenterOf(it) },
            Balloon.Position.atRight)
   }
}
