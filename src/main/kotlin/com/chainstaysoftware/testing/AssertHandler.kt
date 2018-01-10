package com.chainstaysoftware.testing

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethodCallExpression

/**
 * Handler interface for conversion to AssertJ Assertions.
 */
interface AssertHandler {
   /**
    * Handler should return true if the Handler can convert the passed in {@link PsiElement}
    */
   fun canHandle(psiElement: PsiElement): Boolean

   /**
    * Converts the {@link PsiElement}. Will only be called if canHandle return true.
    */
   fun handle(project: Project, psiElement: PsiElement)

   /**
    * Returns true if the passed in {@link PsiElement} is
    * a {@link PsiMethodCallExpression} and has a qualified name that
    * equals the qualifiedClassName param.
    */
   fun isQualifiedClass(psiElement: PsiElement,
                        qualifiedClassName: String): Boolean {
      if (psiElement !is PsiMethodCallExpression) {
         return false
      }

      val resolvedMethod = psiElement.resolveMethod()
      if (resolvedMethod == null || resolvedMethod.containingClass == null) {
         return false
      }

      return qualifiedClassName == resolvedMethod.containingClass!!.qualifiedName
   }
}