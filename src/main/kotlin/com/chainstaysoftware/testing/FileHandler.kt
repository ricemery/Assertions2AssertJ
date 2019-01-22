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
      val imports = mutableSetOf<Pair<String, String>>()

      psiFile.children
         .filterIsInstance<PsiClass>()
         .forEach {

            it.allMethods.forEach { psiMethod ->
               psiMethod.accept(object : PsiRecursiveElementVisitor() {
                  override fun visitElement(psiElement: PsiElement) {
                     val handler = handlers.firstOrNull { handler -> handler.canHandle(psiElement) }
                     when {
                        handler != null -> {
                           val imps = handler.handle(psiFile.project, psiElement)
                           imports.addAll(imps)
                           codeModified = true
                        }
                        else -> super.visitElement(psiElement)
                     }
                  }
               })
            }
         }

      if (codeModified) {
         Util.removeImportIf(psiFile) { it.startsWith("org.hamcrest") }
         Util.removeImportIf(psiFile) { it.startsWith("org.junit.Assert") }
         Util.removeImportIf(psiFile) {
            it.startsWith("org.junit.jupiter.api.Assertions") &&
               !it.startsWith("org.junit.jupiter.api.Assertions.assertAll")
         }

         imports.forEach { import ->
            Util.addStaticImport(psiFile.project, psiFile, import.first, import.second)
         }
      }
   }
}
