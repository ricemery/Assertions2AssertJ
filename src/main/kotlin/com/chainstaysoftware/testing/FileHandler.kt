package com.chainstaysoftware.testing

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor


class FileHandler {
   private val handlers = listOf(HamcrestHandler(), Junit5Handler())

   fun handle(psiFile: PsiFile) {
      var codeModified = false

      for (psiElement in psiFile.children) {
         if (psiElement is PsiClass) {
            psiElement.allMethods.forEach { psiMethod ->
               psiMethod.accept(object : PsiRecursiveElementVisitor() {
                  override fun visitElement(psiElement2: PsiElement) {
                     val handler = handlers.firstOrNull { handler -> handler.canHandle(psiElement2) }
                     when {
                        handler != null -> {
                           handler.handle(psiFile.project, psiElement2)
                           codeModified = true
                        }
                        else -> super.visitElement(psiElement2)
                     }
                  }
               })

            }
         }
      }

      if (codeModified) {
         Util.removeImportStartsWith(psiFile, "org.hamcrest")
         Util.removeImport(psiFile, "org.junit.jupiter.api.Assertions")
         Util.addImport(psiFile.project, psiFile, "org.assertj.core.api.Assertions")
      }
   }
}
