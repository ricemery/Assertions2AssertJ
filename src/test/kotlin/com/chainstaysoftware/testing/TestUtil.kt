package com.chainstaysoftware.testing

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiRecursiveElementVisitor
import org.assertj.core.api.Assertions

object TestUtil {
   /**
    * Gets the first PsiMethodCallExpression from the first Class and First Method
    * of the passed in file.
    */
   fun getPsiMethodCallExpression(myFile: PsiFile): PsiMethodCallExpression? =
      ReadAction.compute<PsiMethodCallExpression?, IllegalStateException> {
         var methodCall: PsiMethodCallExpression? = null

         myFile.children
            .filterIsInstance<PsiClass>()
            .first()
            .allMethods
            .forEach { psiMethod ->
               psiMethod.accept(object : PsiRecursiveElementVisitor() {
                  override fun visitElement(psiElement: PsiElement) {

                     if (psiElement is PsiMethodCallExpression)
                        methodCall = psiElement
                     else
                        super.visitElement(psiElement)
                  }
               })
            }

         methodCall
      }

   fun assertHandle(handler: AssertHandler,
                    project: Project,
                    myFile: PsiFile,
                    expected: String) {
      val psiMethodCallExpression = TestUtil.getPsiMethodCallExpression(myFile)

      WriteCommandAction.runWriteCommandAction(project) {
         if (psiMethodCallExpression == null)
            Assertions.fail("Missing method call")
         else
            handler.handle(project, psiMethodCallExpression)
      }

      ReadAction.run<IllegalStateException> {
         val updated = TestUtil.getPsiMethodCallExpression(myFile)
         if (updated == null)
            Assertions.fail("Missing method call")
         else
            Assertions.assertThat(updated.text).isEqualToIgnoringWhitespace(expected)
      }
   }
}