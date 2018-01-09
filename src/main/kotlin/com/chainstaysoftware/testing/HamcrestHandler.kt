package com.chainstaysoftware.testing

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClassObjectAccessExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiExpressionList
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.impl.source.PsiImmediateClassType

/**
 * Handler to convert Hamcrest Assertions and Junit 4 assertThat Assertions to AssertJ Assertions.
 */
class HamcrestHandler : AssertHandler {
   override fun canHandle(psiElement: PsiElement): Boolean =
      isQualifiedClass(psiElement, "org.hamcrest.MatcherAssert") ||
         (isQualifiedClass(psiElement, "org.junit.Assert") &&
            "assertThat" == Util.getMethodName(psiElement))

   override fun handle(project: Project, psiElement: PsiElement) {
      psiElement.children.forEach { child ->
         refactorHamcrest(project, psiElement, child)
      }
   }

   private fun refactorHamcrest(project: Project,
                                matcherAssertElement: PsiElement,
                                childElement: PsiElement?) {
      if (childElement is PsiExpressionList) {
         val expressions = childElement.expressions
         val newExpressionStr = when {
            expressions.size == 3 -> "Assertions.assertThat(${expressions[1].text.trim()})" +
               ".as(${expressions[0].text.trim()})" +
               ".${refactorAssertCall(expressions[2])}"
            expressions.size == 2 -> "Assertions.assertThat(${expressions[0].text.trim()})" +
               ".${refactorAssertCall(expressions[1])}"
            else -> null
         }

         if (newExpressionStr != null) {
            val elementFactory = JavaPsiFacade.getElementFactory(project)
            val newExpression = elementFactory
               .createStatementFromText(newExpressionStr, null)
            matcherAssertElement.replace(newExpression)
         }
      }
   }

   private fun refactorAssertCall(psiExpression: PsiExpression): String {
      val s = psiExpression.text.trim()
      if (psiExpression !is PsiMethodCallExpression) {
         return s
      }

      val methodName = Util.getMethodName(psiExpression) ?: return s
      val methodParams = (psiExpression.children[1] as PsiExpressionList).expressions

      return when {
         s == "equalTo(true)" || s == "is(true)" || s == "not(false)" -> "isTrue()"
         s == "equalTo(false)" || s == "is(false)" || s == "not(true)" -> "isFalse()"
         s == "not(emptyArray())" || s == "not(emptyIterable())" -> "isNotEmpty()"
         methodName == "equalTo" -> refactor("isEqualTo", methodParams)
         methodName == "equalToIgnoringCase" -> refactor("isEqualToIgnoringCase", methodParams)
         methodName == "equalToIgnoringWhiteSpace" -> refactor("isEqualToIgnoringWhitespace", methodParams)
         methodName == "closeTo" -> refactorAssertCloseTo(methodParams)
         methodName == "hasItems" -> refactor("contains", methodParams)
         methodName == "hasItem" -> refactor("contains", methodParams)
         methodName == "hasEntry" -> refactor("containsKey", methodParams)
         methodName == "containsString" -> refactor("contains", methodParams)
         methodName == "is" -> refactorAssertIs(methodParams)
         methodName == "notNullValue" -> "isNotNull()"
         methodName == "nullValue" -> "isNull()"
         methodName == "not" -> refactorNot(methodParams)
         methodName == "instanceOf" -> refactor("isInstanceOf", methodParams)
         methodName == "any" -> refactor("isInstanceOf", methodParams)
         methodName == "lessThan" -> refactorAssertLessThan(methodParams)
         methodName == "lessThanOrEqualTo" -> refactorAssertLessThan(methodParams, true)
         methodName == "greaterThan" -> refactorAssertGreaterThan(methodParams)
         methodName == "greaterThanOrEqualTo" -> refactorAssertGreaterThan(methodParams, true)
         methodName == "contains" -> refactor("containsExactly", methodParams)
         methodName == "containsInAnyOrder" -> refactor("containsAll", methodParams)
         methodName == "sameInstance" || methodName == "theInstance"-> refactor("isSameAs", methodParams)
         methodName == "startsWith" -> refactor("startsWith", methodParams)
         methodName == "endsWith" -> refactor("endsWith", methodParams)
         methodName == "allOf" || methodName == "array" -> refactorAllOf(methodParams)
         methodName == "arrayContaining" -> refactorArrayContaining(methodParams)
         methodName == "arrayContainingInAnyOrder" -> refactorArrayContainingInAnyOrder(methodParams)
         methodName == "arrayWithSize" -> refactorArrayWithSize(methodParams)
         methodName == "emptyArray" -> "isEmpty()"
         methodName == "emptyIterable" -> "isEmpty()"
         else -> s
      }
   }

   private fun refactor(methodCall: String, expressions: Array<PsiExpression>): String =
      "$methodCall(${expressions.joinToString(", " ){ e -> e.text }})"

   private fun refactorAssertCloseTo(expressions: Array<PsiExpression>): String {
      val expected = expressions[0].text
      val delta = expressions[1].text
      return "isCloseTo(${expected.trim()}, Assertions.offset(${delta.trim()}))"
   }

   // TODO: Better handling for is.
   private fun refactorAssertIs(expressions: Array<PsiExpression>): String =
      when {
         expressions[0] is PsiClassObjectAccessExpression -> "isInstanceOf(${expressions[0].text})"
         expressions[0] is PsiLiteralExpression -> "isEqualTo(${expressions[0].text})"
         else -> {
            val refactored = refactorAssertCall(expressions[0])
            if (expressions[0].text == refactored)
               "isEqualTo(${expressions[0].text})"
            else
               refactored
         }
      }

   // TODO: Better handling for Not
   private fun refactorNot(expressions: Array<PsiExpression>): String =
      refactorAssertCall(expressions[0]).replaceFirst("is", "isNot")

   private fun refactorAssertLessThan(expressions: Array<PsiExpression>,
                                      orEqual: Boolean = false): String {
      return if (isDateOrInstant(expressions[0]))
         refactor(if (orEqual) "isBeforeOrEqualTo" else "isBefore", expressions)
      else
         refactor(if (orEqual) "isLessThanOrEqualTo" else "isLessThan", expressions)
   }

   private fun refactorAssertGreaterThan(expressions: Array<PsiExpression>,
                                         orEqual: Boolean = false): String {
      return if (isDateOrInstant(expressions[0]))
         refactor(if (orEqual) "isAfterOrEqualTo" else "isAfter", expressions)
      else
         refactor(if (orEqual) "isGreaterThanOrEqualTo" else "isGreaterThan", expressions)
   }

   private fun isDateOrInstant(expression: PsiExpression): Boolean {
      val type = expression.type
      return type is PsiImmediateClassType && (type.className == "Date" || type.className == "Instant")
         || (expression is PsiMethodCallExpression
         && ((expression.methodExpression.qualifier as PsiReferenceExpression).text == "Date"
         || (expression.methodExpression.qualifier as PsiReferenceExpression).text == "Instant"))
   }

   private fun refactorAllOf(expressions: Array<PsiExpression>) =
      expressions.joinToString(".") { expression -> refactorAssertCall(expression) }

   // TODO - better support for collection of matchers - current support is not correct
   private fun refactorArrayContaining(expressions: Array<PsiExpression>): String =
      when (expressions[0]) {
         is PsiReferenceExpression, is PsiLiteralExpression -> "containsExactly(${expressions
            .joinToString(", ") { expression -> expression.text }})"
         else -> "containsExactly(${expressions
            .joinToString(".") { expression -> refactorAssertCall(expression) }})"
      }

   // TODO - better support for collection of matchers - current support is not correct
   private fun refactorArrayContainingInAnyOrder(expressions: Array<PsiExpression>): String =
      when (expressions[0]) {
         is PsiReferenceExpression, is PsiLiteralExpression -> "contains(${expressions
            .joinToString(", ") { expression -> expression.text }})"
         else -> "contains(${expressions
            .joinToString(".") { expression -> refactorAssertCall(expression) }})"
      }

   // TODO - better support for collection of matchers - current support is not correct
   private fun refactorArrayWithSize(expressions: Array<PsiExpression>): String =
      when (expressions[0]) {
         is PsiReferenceExpression, is PsiLiteralExpression -> "hasSize(${expressions[0].text})"
         else -> "hasSize(${expressions
            .joinToString(".") { expression -> refactorAssertCall(expression) }})"
      }
}