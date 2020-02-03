package com.chainstaysoftware.testing

import com.chainstaysoftware.testing.Util.isQualifiedClass
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiExpressionList

/**
 * Handler to convert Junit Assertions (excluding assertThat) to AssertJ Assertions.
 */
class JunitHandler : AssertHandler {
   private val refactorMap: Map<String, (Boolean, Array<PsiExpression>) -> String?>
      = mapOf("assertEquals" to { junit4, expressions -> refactorAssertEquals(junit4, expressions) },
      "assertNotEquals" to { junit4, expressions -> refactorAssertNotEquals(junit4, expressions) },
      "assertSame" to { junit4, expressions -> refactorAssertSameAs(junit4, expressions) },
      "assertNotSame" to { junit4, expressions -> refactorAssertNotSameAs(junit4, expressions) },
      "assertArrayEquals" to { junit4, expressions -> refactorAssertArrayEquals(junit4, expressions) },
      "assertIterableEquals" to { _, expressions -> refactorAssertIterableEquals(expressions) },
      "assertLinesMatch" to { _, expressions -> refactorAssertIterableEquals(expressions) },
      "assertTrue" to { junit4, expressions -> refactorAssertTrue(junit4, expressions) },
      "assertFalse" to { junit4, expressions -> refactorAssertFalse(junit4, expressions) },
      "assertNull" to { junit4, expressions -> refactorAssertNull(junit4, expressions) },
      "assertNotNull" to { junit4, expressions -> refactorAssertNotNull(junit4, expressions) },
      "assertThrows" to { _, expressions -> refactorAssertThrows(expressions) },
      "fail" to { _, expressions -> refactorFail(expressions) })

   override fun canHandle(psiElement: PsiElement): Boolean =
      isQualifiedClass(psiElement, "org.junit.jupiter.api.Assertions") &&
         "assertAll" != Util.getMethodName(psiElement) ||
              (isQualifiedClass(psiElement, "org.junit.Assert") ||
                      isQualifiedClass(psiElement, "junit.framework.TestCase") ||
                      isQualifiedClass(psiElement, "junit.extensions.TestDecorator") ||
                      isQualifiedClass(psiElement, "junit.tests.runner.ClassLoaderTest") ||
                      isQualifiedClass(psiElement, "junit.tests.runner.ClassLoaderTest")) &&
         "assertThat" != Util.getMethodName(psiElement)


   override fun handle(project: Project, psiElement: PsiElement): Set<Pair<String, String>> =
      psiElement.children.map { child ->
            refactorJunit(project, psiElement, child)
         }
         .flatten()
         .toSet()

   private fun refactorJunit(project: Project,
                             junitAssertElement: PsiElement,
                             childElement: PsiElement?): Set<Pair<String, String>> {
      val emptyImports = hashSetOf<Pair<String, String>>()
      if (childElement is PsiExpressionList) {
         val methodName = Util.getMethodName(junitAssertElement) ?: return emptyImports

         val isJunit4 = Util.getClassName(junitAssertElement) == "Assert"
         val expressions = childElement.expressions
         val newExpressionStr = refactorMap.getOrDefault(methodName, { _, _ -> null })
            .invoke(isJunit4, expressions) ?: return emptyImports

         val elementFactory = JavaPsiFacade.getElementFactory(project)
         val newExpression = elementFactory
            .createStatementFromText(newExpressionStr, null)
         junitAssertElement.replace(newExpression)

         return getStaticImports(newExpression)
      } else {
         return emptyImports
      }
   }

   private fun refactorAssertEquals(junit4: Boolean, expressions: Array<PsiExpression>): String {
      return when {
         junit4 && expressions.size == 4 -> {
            val expected = expressions[1].text
            val actual = expressions[2].text
            val delta = expressions[3].text
            val desc = expressions[0].text
            assertStr(actual, "isCloseTo(${expected.trim()}, offset(${delta.trim()}))", desc)
         }
         expressions.size == 4 -> {
            val expected = if (junit4) expressions[1].text else expressions[0].text
            val actual = if (junit4) expressions[2].text else expressions[1].text
            val delta = if (junit4) expressions[3].text else expressions[2].text
            val desc = if (junit4) expressions[0].text else expressions[3].text
            assertStr(actual, "isCloseTo(${expected.trim()}, offset(${delta.trim()}))", desc)
         }
         junit4 && expressions.size == 3 -> {
            if ("PsiType:String" == expressions[0].type.toString()) {
               val desc = expressions[0].text
               val expected = expressions[1].text
               val actual = expressions[2].text
               assertStr(actual, "isEqualTo(" + expected.trim() + ")", desc)
            } else {
               val expected = expressions[0].text
               val actual = expressions[1].text
               val delta = expressions[2].text
               assertStr(actual, "isCloseTo(${expected.trim()}, offset(${delta.trim()}))")
            }
         }
         expressions.size == 3 -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            if ("PsiType:String" == expressions[2].type.toString()) {
               val desc = expressions[2].text
               assertStr(actual, "isEqualTo(" + expected.trim() + ")", desc)
            } else {
               val delta = expressions[2].text
               assertStr(actual, "isCloseTo(${expected.trim()}, offset(${delta.trim()}))")
            }
         }
         else -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            assertStr(actual, "isEqualTo($expected)")
         }
      }
   }

   private fun refactorAssertNotSameAs(junit4: Boolean, expressions: Array<PsiExpression>): String {
      return when {
         junit4 && expressions.size == 3 -> {
            val desc = expressions[0].text
            val expected = expressions[1].text
            val actual = expressions[2].text
            assertStr(actual, "isNotSameAs(" + expected.trim() + ")", desc)
         }
         expressions.size == 3 -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            val desc = expressions[2].text
            assertStr(actual, "isNotSameAs(" + expected.trim() + ")", desc)
         }
         else -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            assertStr(actual, "isNotSameAs($expected)")
         }
      }
   }


   private fun refactorAssertSameAs(junit4: Boolean, expressions: Array<PsiExpression>): String {
      return when {
         junit4 && expressions.size == 3 -> {
            val desc = expressions[0].text
            val expected = expressions[1].text
            val actual = expressions[2].text
            assertStr(actual, "isSameAs(" + expected.trim() + ")", desc)
         }
         expressions.size == 3 -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            val desc = expressions[2].text
            assertStr(actual, "isSameAs(" + expected.trim() + ")", desc)
         }
         else -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            assertStr(actual, "isSameAs($expected)")
         }
      }
   }

   private fun refactorAssertNotEquals(junit4: Boolean, expressions: Array<PsiExpression>): String {
      return when {
         junit4 && expressions.size == 3 -> {
            val desc = expressions[0].text
            val expected = expressions[1].text
            val actual = expressions[2].text
            assertStr(actual, "isNotEqualTo(" + expected.trim() + ")", desc)
         }
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

   private fun refactorAssertArrayEquals(junit4: Boolean, expressions: Array<PsiExpression>): String {
      return when {
         junit4 && expressions.size == 4 -> {
            val desc = expressions[0].text
            val expected = expressions[1].text
            val actual = expressions[2].text
            val delta = expressions[3].text
            assertStr(actual, "contains(${expected.trim()}, offset(${delta.trim()}))", desc)
         }
         expressions.size == 4 -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            val delta = expressions[2].text
            val desc = expressions[3].text
            assertStr(actual, "contains(${expected.trim()}, offset(${delta.trim()}))", desc)
         }
         junit4 && expressions.size == 3 -> {
            if ("PsiType:String" == expressions[0].type.toString()) {
               val desc = expressions[0].text
               val expected = expressions[1].text
               val actual = expressions[2].text
               assertStr(actual, "isEqualTo(" + expected.trim() + ")", desc)
            } else {
               val expected = expressions[0].text
               val actual = expressions[1].text
               val delta = expressions[2].text
               assertStr(actual, "contains(${expected.trim()}, offset(${delta.trim()}))")
            }
         }
         expressions.size == 3 -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            if ("PsiType:String" == expressions[2].type.toString()) {
               val desc = expressions[2].text
               assertStr(actual, "isEqualTo(" + expected.trim() + ")", desc)
            } else {
               val delta = expressions[2].text
               assertStr(actual, "contains(${expected.trim()}, offset(${delta.trim()}))")
            }
         }
         else -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            assertStr(actual, "isEqualTo($expected)")
         }
      }
   }

   private fun refactorAssertIterableEquals(expressions: Array<PsiExpression>): String {
      return when {
         expressions.size == 3 -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            val desc = expressions[2].text
            assertStr(actual, "isEqualTo(" + expected.trim() + ")", desc)
         }
         else -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            assertStr(actual, "isEqualTo($expected)")
         }
      }
   }

   private fun refactorAssertTrue(junit4: Boolean, expressions: Array<PsiExpression>): String {
      return when {
         junit4 && expressions.size == 2 -> {
            val desc = expressions[0].text
            val actual = expressions[1].text
            assertStr(actual, "isTrue()", desc)
         }
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

   private fun refactorAssertFalse(junit4: Boolean, expressions: Array<PsiExpression>): String {
      return when {
         junit4 && expressions.size == 2 -> {
            val desc = expressions[0].text
            val actual = expressions[1].text
            assertStr(actual, "isFalse()", desc)
         }
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

   private fun refactorAssertNull(junit4: Boolean, expressions: Array<PsiExpression>): String {
      return when {
         junit4 && expressions.size == 2 -> {
            val desc = expressions[0].text
            val actual = expressions[1].text
            assertStr(actual, "isNull()", desc)
         }
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

   private fun refactorAssertNotNull(junit4: Boolean, expressions: Array<PsiExpression>): String {
      return when {
         junit4 && expressions.size == 2 -> {
            val desc = expressions[0].text
            val actual = expressions[1].text
            assertStr(actual, "isNotNull()", desc)
         }
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
            "assertThatExceptionOfType(${expected.trim()}).as(${desc.trim()}).isThrownBy(${actual.trim()})"
         }
         else -> {
            val expected = expressions[0].text
            val actual = expressions[1].text
            "assertThatExceptionOfType(${expected.trim()}).isThrownBy(${actual.trim()})"
         }
      }
   }

   private fun refactorFail(expressions: Array<PsiExpression>): String {
      return when {
         expressions.size == 2 -> {
            val cause = expressions[0].text
            val desc = expressions[1].text
            "fail($desc, ${cause.trim()})"
         }
         expressions.size == 1 -> "fail(${expressions[0].text.trim()})"
         else -> "fail()"
      }
   }

   private fun assertStr(actual: String,
                         assertExpression: String,
                         description: String? = null) =
      if (description == null)
         "assertThat(${actual.trim()}).$assertExpression"
      else
         "assertThat(${actual.trim()}).as(${description.trim()}).$assertExpression"
}