package com.chainstaysoftware.testing

import com.chainstaysoftware.testing.Util.isQualifiedClass
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
import com.intellij.psi.util.PsiTreeUtil

/**
 * Handler to convert Hamcrest Assertions and Junit 4 assertThat Assertions to AssertJ Assertions.
 */
class HamcrestHandler : AssertHandler {
   // HashSet of 'recursive' matcher calls that will be supported. All others will be
   // ignored.
   private val okRecursive = hashSetOf("is(",
      "not(equalTo",
      "not(empty())",
      "not(emptyCollectionOf(",
      "not(emptyArray())",
      "not(emptyIterable())",
      "not(instanceOf(",
      "not(isEmptyString())",
      "not(isEmptyOrNullString())",
      "allOf(",
      "both(",
      "describedAs(")

   override fun canHandle(psiElement: PsiElement): Boolean =
      (isQualifiedClass(psiElement, "org.hamcrest.MatcherAssert") ||
         (isQualifiedClass(psiElement, "org.junit.Assert") &&
            "assertThat" == Util.getMethodName(psiElement)))
      && !shouldIgnore(psiElement)

   /**
    * True if the passed in PsiElement should be ignored. Most Assert statements
    * that contain Matchers within Matchers will be ignored. Example - anyOf(...)
    */
   private fun shouldIgnore(psiElement: PsiElement): Boolean {
      if (!hasRecursiveMatchers(psiElement)) {
         return false
      }

      val expressions = psiElement.children
         .filter { child -> child is PsiExpressionList }
         .map { child -> (child as PsiExpressionList).expressions }
         .first()
      val matcher = expressions[expressions.size - 1]
      return okRecursive.find { prefix -> matcher.text.startsWith(prefix) } == null
   }

   /**
    * Determines if the passed in PsiElement contains a tree of Matchers.
    */
   private fun hasRecursiveMatchers(psiElement: PsiElement) =
      psiElement.children
         .filter { child -> child is PsiExpressionList }
         .map { child -> (child as PsiExpressionList).expressions }
         .map { expressions -> hasChildMatcher(expressions[expressions.size - 1]) }
         .contains(true)

   /**
    * Determines if a PsiElement has a child that is a CoreMatchers or Matchers instance.
    */
   private fun hasChildMatcher(psiElement: PsiElement): Boolean =
      PsiTreeUtil.findChildrenOfType(psiElement, PsiMethodCallExpression::class.java)
         .any { elem -> isQualifiedClass(elem, "org.hamcrest.CoreMatchers")
            || isQualifiedClass(elem, "org.hamcrest.Matchers")}

   override fun handle(project: Project, psiElement: PsiElement): Set<Pair<String, String>> {
      val imports = mutableSetOf<Pair<String, String>>()

      psiElement.children
         .filter { child -> child is PsiExpressionList }
         .forEach { child ->
            val imps = refactorHamcrest(project, psiElement, child as PsiExpressionList)
            imports.addAll(imps)
         }

      return imports
   }

   private fun refactorHamcrest(project: Project,
                                matcherAssertElement: PsiElement,
                                childElement: PsiExpressionList): Set<Pair<String, String>> {
         val expressions = childElement.expressions
         val newExpressionStr = when {
            expressions.size == 3 -> "assertThat(${expressions[1].text.trim()})" +
               ".as(${expressions[0].text.trim()})" +
               ".${refactorAssertCall(expressions[2])}"
            expressions.size == 2 -> "assertThat(${expressions[0].text.trim()})" +
               ".${refactorAssertCall(expressions[1])}"
            else -> null
         }

         if (newExpressionStr != null) {
            val elementFactory = JavaPsiFacade.getElementFactory(project)
            val newExpression = elementFactory
               .createStatementFromText(newExpressionStr, null)
            matcherAssertElement.replace(newExpression)

            return getStaticImports(newExpression)
         } else {
            return hashSetOf()
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
         s == "not(isEmptyString())" -> "isNotEmpty()"
         s == "not(isEmptyOrNullString())" -> "isNotBlank()"
         methodName == "equalTo" -> refactor("isEqualTo", methodParams)
         methodName == "equalToIgnoringCase" -> refactor("isEqualToIgnoringCase", methodParams)
         methodName == "equalToIgnoringWhiteSpace" -> refactor("isEqualToIgnoringWhitespace", methodParams)
         methodName == "closeTo" -> refactorAssertCloseTo(methodParams)
         methodName == "hasItems" -> refactor("contains", methodParams)
         methodName == "hasItem" -> refactor("contains", methodParams)
         methodName == "hasEntry" -> refactor("containsEntry", methodParams)
         methodName == "hasKey" -> refactor("containsKey", methodParams)
         methodName == "hasValue" -> refactor("containsValue", methodParams)
         methodName == "hasToString" -> refactor("hasToString", methodParams)
         methodName == "containsString" -> refactor("contains", methodParams)
         methodName == "is" -> refactorAssertIs(methodParams)
         methodName == "isEmptyString" -> refactor("isEmpty", methodParams)
         methodName == "isEmptyOrNullString" -> refactor("isBlank", methodParams)
         methodName == "notNullValue" -> "isNotNull()"
         methodName == "nullValue" -> "isNull()"
         methodName == "not" -> refactorNot(methodParams)
         methodName == "instanceOf" || methodName == "typeCompatibleWith" -> refactor("isInstanceOf", methodParams)
         methodName == "any" -> refactor("isInstanceOf", methodParams)
         methodName == "lessThan" -> refactorAssertLessThan(methodParams)
         methodName == "lessThanOrEqualTo" -> refactorAssertLessThan(methodParams, true)
         methodName == "greaterThan" -> refactorAssertGreaterThan(methodParams)
         methodName == "greaterThanOrEqualTo" -> refactorAssertGreaterThan(methodParams, true)
         methodName == "contains" -> refactor("containsExactly", methodParams)
         methodName == "containsInAnyOrder" -> refactor("contains", methodParams)
         methodName == "sameInstance" || methodName == "theInstance"-> refactor("isSameAs", methodParams)
         methodName == "startsWith" -> refactor("startsWith", methodParams)
         methodName == "endsWith" -> refactor("endsWith", methodParams)
         methodName == "allOf" || methodName == "array" || methodName == "both" -> refactorAllOf(methodParams)
         methodName == "arrayContaining" -> refactorArrayContaining(methodParams)
         methodName == "arrayContainingInAnyOrder" -> refactorArrayContainingInAnyOrder(methodParams)
         methodName == "arrayWithSize" -> refactorArrayWithSize(methodParams)
         methodName == "hasSize" || methodName == "iterableWithSize" -> refactorHasSize(methodParams)
         methodName == "empty" -> "isEmpty()"
         methodName == "emptyArray" -> "isEmpty()"
         methodName == "emptyIterable" -> "isEmpty()"
         methodName == "emptyCollectionOf" -> "isEmpty()"
         methodName == "describedAs" -> refactorDescribedAs(methodParams)
         else -> s
      }
   }

   private fun refactor(methodCall: String, expressions: Array<PsiExpression>): String =
      "$methodCall(${expressions.joinToString(", " ){ e -> e.text }})"

   private fun refactorAssertCloseTo(expressions: Array<PsiExpression>): String {
      val expected = expressions[0].text
      val delta = expressions[1].text
      return "isCloseTo(${expected.trim()}, offset(${delta.trim()}))"
   }

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

   private fun refactorNot(expressions: Array<PsiExpression>): String =
      refactorAssertIs(expressions).replaceFirst("is", "isNot")

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

   private fun refactorArrayContaining(expressions: Array<PsiExpression>): String =
      when (expressions[0]) {
         is PsiReferenceExpression, is PsiLiteralExpression -> "containsExactly(${expressions
            .joinToString(", ") { expression -> expression.text }})"
         else -> "containsExactly(${expressions
            .joinToString(".") { expression -> refactorAssertCall(expression) }})"
      }

   private fun refactorArrayContainingInAnyOrder(expressions: Array<PsiExpression>): String =
      when (expressions[0]) {
         is PsiReferenceExpression, is PsiLiteralExpression -> "contains(${expressions
            .joinToString(", ") { expression -> expression.text }})"
         else -> "contains(${expressions
            .joinToString(".") { expression -> refactorAssertCall(expression) }})"
      }

   private fun refactorArrayWithSize(expressions: Array<PsiExpression>): String =
      when (expressions[0]) {
         is PsiReferenceExpression, is PsiLiteralExpression -> "hasSize(${expressions[0].text})"
         else -> "hasSize(${expressions
            .joinToString(".") { expression -> refactorAssertCall(expression) }})"
      }

   private fun refactorHasSize(expressions: Array<PsiExpression>): String =
      when (expressions[0]) {
         is PsiReferenceExpression, is PsiLiteralExpression -> "hasSize(${expressions[0].text})"
         else -> "hasSize(${expressions
            .joinToString(".") { expression -> refactorAssertCall(expression) }})"
      }

   private fun refactorDescribedAs(expressions: Array<PsiExpression>): String {
      val args = expressions.copyOfRange(2, expressions.size).joinToString(", ") { arg -> arg.text }
      return "as(${expressions[0].text}, $args)." + refactorAssertCall(expressions[1])
   }
}