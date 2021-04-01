package com.chainstaysoftware.testing

import com.intellij.openapi.project.Project
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
      val project = psiFile.project

      psiFile.children
         .filterIsInstance<PsiClass>()
         .forEach {
            val result = handlePsiClass(it, project)
            codeModified = codeModified || result.first
            imports.addAll(result.second)
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

   /**
    * Handle conversion of asserts within a class. Recurses to handle inner classes.
    * Returns a Pair where the left value indicates if any statements where modified
    * and the right value is a set of imports that will need to be added to the class.
    */
   private fun handlePsiClass(
      psiClass: PsiClass,
      project: Project,
   ): Pair<Boolean, Set<Pair<String, String>>> {
      var codeModified = false
      val imports = mutableSetOf<Pair<String, String>>()

      psiClass.allInnerClasses.forEach {
         val result = handlePsiClass(it, project)
         codeModified = codeModified || result.first
         imports.addAll(result.second)
      }

      psiClass.allMethods.forEach { psiMethod ->
         psiMethod.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(psiElement: PsiElement) {
               val handler =
                  handlers.firstOrNull { handler -> handler.canHandle(psiElement) }
               when {
                  handler != null -> {
                     val imps = handler.handle(project, psiElement)
                     imports.addAll(imps)
                     codeModified = true
                  }
                  else -> super.visitElement(psiElement)
               }
            }
         })
      }

      return Pair(codeModified, imports)
   }
}
