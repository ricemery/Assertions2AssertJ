package com.chainstaysoftware.testing

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor


/**
 * Handle converting a single Java file from Hamcrest and Junit Assertions
 * to AssertJ.
 */
class FileHandler {
   private val handlers = listOf(HamcrestHandler(), JunitHandler())

   fun handle(psiFile: PsiFile) {
      var codeModified = false

      psiFile.children
         .filterIsInstance<PsiClass>()
         .forEach {
            it.allMethods.forEach { psiMethod ->
               psiMethod.accept(object : PsiRecursiveElementVisitor() {
                  override fun visitElement(psiElement: PsiElement) {
                     val handler = handlers.firstOrNull { handler -> handler.canHandle(psiElement) }
                     when {
                        handler != null -> {
                           handler.handle(psiFile.project, psiElement)
                           codeModified = true
                        }
                        else -> super.visitElement(psiElement)
                     }
                  }
               })
            }
         }

      if (codeModified) {
         Util.removeImportStartsWith(psiFile, "org.hamcrest")
         Util.removeImportStartsWith(psiFile, "org.junit.Assert")
         Util.removeImport(psiFile, "org.junit.jupiter.api.Assertions")
         Util.addImport(psiFile.project, psiFile, "org.assertj.core.api.Assertions")
      }
   }
}
