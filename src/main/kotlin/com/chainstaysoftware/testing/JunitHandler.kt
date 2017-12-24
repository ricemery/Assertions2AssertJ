package com.chainstaysoftware.testing

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiExpressionList
import com.intellij.psi.PsiType

/**
 * Handler to convert Junit Assertions (excluding assertThat) to AssertJ Assertions.
 */
class JunitHandler : AssertHandler {
   private val refactorMap: Map<String, (Array<PsiExpression>) -> String?>
      = mapOf("assertEquals" to { expression -> refactorAssertEquals(expression) },
      "assertNotEquals" to { expression -> refactorAssertNotEquals(expression) },
      "assertSame" to { expression -> refactorAssertEquals(expression) },
      "assertNotSame" to { expression -> refactorAssertNotEquals(expression) },
      "assertArrayEquals" to { expression -> refactorAssertArrayEquals(expression) },
      "assertTrue" to { expression -> refactorAssertTrue(expression) },
      "assertFalse" to { expression -> refactorAssertFalse(expression) },
      "assertNull" to { expression -> refactorAssertNull(expression) },
      "assertNotNull" to { expression -> refactorAssertNotNull(expression) },
      "assertThrows" to { expression -> refactorAssertThrows(expression) })

   override fun canHandle(psiElement: PsiElement): Boolean =
      isQualifiedClass(psiElement, "org.junit.jupiter.api.Assertions") ||
         (isQualifiedClass(psiElement, "org.junit.Assert") &&
            "assertThat" != Util.getMethodName(psiElement))


   override fun handle(project: Project, psiElement: PsiElement) {
      psiElement.children.forEach { child ->
         refactorJunit5(project, psiElement, child)
      }
   }

   private fun refactorJunit5(project: Project,
                              junitAssertElement: PsiElement,
                              childElement: PsiElement?) {
      if (childElement is PsiExpressionList) {
         val methodName = Util.getMethodName(junitAssertElement) ?: return

         val expressions = childElement.expressions
         val newExpressionStr = refactorMap.getOrDefault(methodName, { _ -> null })
            .invoke(expressions)


         if (newExpressionStr != null) {
            val elementFactory = JavaPsiFacade.getElementFactory(project)
            val newExpression = elementFactory
               .createStatementFromText(newExpressionStr, null)
            junitAssertElement.replace(newExpression)
         }
      }
   }

   private fun refactorAssertEquals(expressions: Array<PsiExpression>): String {
      return when {
         expressions.size == 4 -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            val delta = expressions[2].text
            val desc = expressions[3].text
            assertStr(actual, "isCloseTo(${expected.trim()}, Assertions.offset(${delta.trim()}))", desc)
         }
         expressions.size == 3 -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            if (expressions[2].type == PsiType.DOUBLE) {
               val delta = expressions[2].text
               assertStr(actual, "isCloseTo(${expected.trim()}, Assertions.offset(${delta.trim()}))")
            } else {
               val desc = expressions[2].text
               assertStr(actual, "isEqualTo(" + expected.trim() + ")", desc)
            }
         }
         else -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            assertStr(actual, "isEqualTo($expected)")
         }
      }
   }

   private fun refactorAssertNotEquals(expressions: Array<PsiExpression>): String {
      return when {
         expressions.size == 3 -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            val desc = expressions[2].text
            assertStr(actual, "isNotEqualTo(" + expected.trim() + ")", desc)
         }
         else -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            assertStr(actual, "isNotEqualTo($expected)")
         }
      }
   }

   private fun refactorAssertArrayEquals(expressions: Array<PsiExpression>): String {
      return when {
         expressions.size == 4 -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            val delta = expressions[2].text
            val desc = expressions[3].text
            assertStr(actual, "contains(${expected.trim()}, Assertions.offset(${delta.trim()}))", desc)
         }
         expressions.size == 3 -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            if (expressions[2].type == PsiType.DOUBLE) {
               val delta = expressions[2].text
               assertStr(actual, "contains(${expected.trim()}, Assertions.offset(${delta.trim()}))")
            } else {
               val desc = expressions[2].text
               assertStr(actual, "isEqualTo(" + expected.trim() + ")", desc)
            }
         }
         else -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            assertStr(actual, "isEqualTo($expected)")
         }
      }
   }

   private fun refactorAssertTrue(expressions: Array<PsiExpression>): String {
      return when {
         expressions.size == 2 -> {
            val actual = expressions[0].text
            val desc = expressions[1].text
            assertStr(actual, "isTrue()", desc)
         }
         else -> {
            val actual = expressions[0].text
            assertStr(actual, "isTrue()")
         }
      }
   }

   private fun refactorAssertFalse(expressions: Array<PsiExpression>): String {
      return when {
         expressions.size == 2 -> {
            val actual = expressions[0].text
            val desc = expressions[1].text
            assertStr(actual, "isFalse()", desc)
         }
         else -> {
            val actual = expressions[0].text
            assertStr(actual, "isFalse()")
         }
      }
   }

   private fun refactorAssertNull(expressions: Array<PsiExpression>): String {
      return when {
         expressions.size == 2 -> {
            val actual = expressions[0].text
            val desc = expressions[1].text
            assertStr(actual, "isNull()", desc)
         }
         else -> {
            val actual = expressions[0].text
            assertStr(actual, "isNull()")
         }
      }
   }

   private fun refactorAssertNotNull(expressions: Array<PsiExpression>): String {
      return when {
         expressions.size == 2 -> {
            val actual = expressions[0].text
            val desc = expressions[1].text
            assertStr(actual, "isNotNull()", desc)
         }
         else -> {
            val actual = expressions[0].text
            assertStr(actual, "isNotNull()")
         }
      }
   }

   private fun refactorAssertThrows(expressions: Array<PsiExpression>): String {
      return when {
         expressions.size == 3 -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            val desc = expressions[2].text
            "Assertions.assertThatExceptionOfType(${expected.trim()}).as(${desc.trim()}).isThrownBy(${actual.trim()})"
         }
         else -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            "Assertions.assertThatExceptionOfType(${expected.trim()}).isThrownBy(${actual.trim()})"
         }
      }
   }

   private fun assertStr(actual: String,
                         assertExpression: String,
                         description: String? = null) =
      if (description == null)
         "Assertions.assertThat(${actual.trim()}).$assertExpression"
      else
         "Assertions.assertThat(${actual.trim()}).as(${description.trim()}).$assertExpression"
}