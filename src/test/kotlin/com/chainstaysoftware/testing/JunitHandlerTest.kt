package com.chainstaysoftware.testing

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase
import com.intellij.util.PathUtil
import org.assertj.core.api.Assertions
import org.junit.Assert
import java.io.File


class JunitHandlerTest : JavaCodeInsightFixtureTestCase() {

   override fun setUp() {
      super.setUp()

      val pathForJunit5 = PathUtil.getJarPathForClass(org.junit.jupiter.api.Assertions::class.java)
      PsiTestUtil.addLibrary(module, "junit5", StringUtil.getPackageName(pathForJunit5, File.separatorChar),
         StringUtil.getShortName(pathForJunit5, File.separatorChar))

      val pathForJunit4 = PathUtil.getJarPathForClass(Assert::class.java)
      PsiTestUtil.addLibrary(module, "junit4", StringUtil.getPackageName(pathForJunit4, File.separatorChar),
         StringUtil.getShortName(pathForJunit4, File.separatorChar))
   }

   fun testtestCanHandleAssertions()  {
      assertCanHandle("org.junit.jupiter.api.Assertions.assertEquals",
         "assertEquals(2, 2)")
   }

   fun testtestCanHandleAssertEquals()  {
      assertCanHandle("org.junit.Assert.assertEquals",
         "assertEquals(2, 2)")
   }

   fun testtestCantHandleAssertThat()  {
      assertCantHandle("org.junit.Assert.assertThat",
         "assertThat(1, equalTo(2))")
   }

   fun testtestCantHandleAssertAll()  {
      assertCantHandle("org.junit.Assert.assertAll",
         "assertAll(() -> assertEquals(2,2))")
   }

   fun testtestHandleAssertEquals()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertEquals",
         "assertEquals(2, 2)",
         "assertThat(2).isEqualTo(2)")
   }

   fun testtestHandleAssertEquals_withDesc()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertEquals",
         "assertEquals(2, 2, \"desc\")",
         "assertThat(2).as(\"desc\").isEqualTo(2)")
   }

   fun testtestHandleAssertEquals_junit4_withDesc()  {
      assertHandle("org.junit.Assert.assertEquals",
         "assertEquals(\"desc\", 2, 2)",
         "assertThat(2).as(\"desc\").isEqualTo(2)")
   }

   fun testhandleAssertEquals_withDelta()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertEquals",
         "assertEquals(2.0, 2.0, 1.0)",
         "assertThat(2.0).isCloseTo(2.0, offset(1.0))")
   }

   fun testhandleAssertEquals_junit4_withDelta()  {
      assertHandle("org.junit.Assert.assertEquals",
         "assertEquals(2.0, 2.0, 1.0)",
         "assertThat(2.0).isCloseTo(2.0, offset(1.0))")
   }

   fun testhandleAssertEquals_withDeltaAndDesc()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertEquals",
         "assertEquals(1.0, 1.0, 0.1, \"desc\")",
         "assertThat(1.0).as(\"desc\").isCloseTo(1.0, offset(0.1))")
   }

   fun testhandleAssertEquals_junit4_withDeltaAndDesc()  {
      assertHandle("org.junit.Assert.assertEquals",
         "assertEquals(\"desc\", 1.0, 1.0, 0.1)",
         "assertThat(1.0).as(\"desc\").isCloseTo(1.0, offset(0.1))")
   }

   fun testhandleAssertNotEquals()  {
      assertHandle("org.junit.Assert.assertNotEquals",
         "assertNotEquals(2, 2)",
         "assertThat(2).isNotEqualTo(2)")
   }

   fun testhandleAssertNotEquals_withDesc()  {
      assertHandle("org.junit.Assert.assertNotEquals",
         "assertNotEquals(2, 2, \"desc\")",
         "assertThat(2).as(\"desc\").isNotEqualTo(2)")
   }

   fun testhandleAssertSame()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertSame",
         "assertSame(2, 2)",
         "assertThat(2).isSameAs(2)")
   }

   fun testhandleAssertSame_junit4()  {
      assertHandle("org.junit.Assert.assertSame",
         "assertSame(2, 2)",
         "assertThat(2).isSameAs(2)")
   }

   fun testhandleAssertSame_withDesc()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertSame",
         "assertSame(2, 2, \"foo\")",
         "assertThat(2).as(\"foo\").isSameAs(2)")
   }

   fun testhandleAssertSame_junit4_withDesc()  {
      assertHandle("org.junit.Assert.assertSame",
         "assertSame(\"foo\", 2, 2)",
         "assertThat(2).as(\"foo\").isSameAs(2)")
   }

   fun testhandleAssertNotSame()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertNotSame",
         "assertNotSame(2, 2)",
         "assertThat(2).isNotSameAs(2)")
   }

   fun testhandleAssertNotSame_junit4()  {
      assertHandle("org.junit.Assert.assertNotSame",
         "assertNotSame(2, 2)",
         "assertThat(2).isNotSameAs(2)")
   }

   fun testhandleAssertNotSame_withDesc()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertNotSame",
         "assertNotSame(2, 2, \"foo\")",
         "assertThat(2).as(\"foo\").isNotSameAs(2)")
   }

   fun testhandleAssertNotSame_junit4_withDesc()  {
      assertHandle("org.junit.Assert.assertNotSame",
         "assertNotSame(\"foo\", 2, 2)",
         "assertThat(2).as(\"foo\").isNotSameAs(2)")
   }

   fun testhandleAssertArrayEquals()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertEquals",
         "assertArrayEquals(new int[]{}, new int[]{})",
         "assertThat(new int[]{}).isEqualTo(new int[]{})")
   }

   fun testhandleAssertArrayEquals_junit4()  {
      assertHandle("org.junit.Assert.assertArrayEquals",
         "assertArrayEquals(new int[]{}, new int[]{})",
         "assertThat(new int[]{}).isEqualTo(new int[]{})")
   }

   fun testhandleAssertArrayEquals_withDesc()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertArrayEquals",
         "assertArrayEquals(new int[]{}, new int[]{}, \"desc\")",
         "assertThat(new int[]{}).as(\"desc\").isEqualTo(new int[]{})")
   }

   fun testhandleAssertArrayEquals_junit4_withDesc()  {
      assertHandle("org.junit.Assert.assertArrayEquals",
         "assertArrayEquals(\"desc\", new int[]{}, new int[]{})",
         "assertThat(new int[]{}).as(\"desc\").isEqualTo(new int[]{})")
   }

   fun testhandleAssertArrayEquals_withDelta()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertArrayEquals",
         "assertArrayEquals(new int[]{}, new int[]{}, 1.0)",
         "assertThat(new int[]{}).contains(new int[]{}, offset(1.0))")
   }

   fun testhandleAssertArrayEquals_junit4_withDelta()  {
      assertHandle("org.junit.Assert.assertArrayEquals",
         "assertArrayEquals(new int[]{}, new int[]{}, 1.0)",
         "assertThat(new int[]{}).contains(new int[]{}, offset(1.0))")
   }

   fun testhandleAssertArrayEquals_withDeltaAndDesc()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertArrayEquals",
         "assertArrayEquals(new int[]{}, new int[]{}, 1.0, \"desc\")",
         "assertThat(new int[]{}).as(\"desc\").contains(new int[]{}, offset(1.0))")
   }

   fun testhandleAssertArrayEquals_junit4_withDeltaAndDesc()  {
      assertHandle("org.junit.Assert.assertArrayEquals",
         "assertArrayEquals(\"desc\", new double[]{}, new double[]{}, 1.0)",
         "assertThat(new double[]{}).as(\"desc\").contains(new double[]{}, offset(1.0))")
   }

   fun testhandleAssertIterableEquals()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertIterableEquals",
         "assertIterableEquals(new List(), new List())",
         "assertThat(new List()).isEqualTo(new List())")
   }

   fun testhandleAssertIterableEquals_withDesc()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertIterableEquals",
         "assertIterableEquals(new List(), new List(), \"desc\")",
         "assertThat(new List()).as(\"desc\").isEqualTo(new List())")
   }

   fun testhandleAssertLinesMatch()  {
      assertHandle("org.junit.Assert.assertLinesMatch",
         "assertLinesMatch(new List(), new List())",
         "assertThat(new List()).isEqualTo(new List())")
   }

   fun testhandleAssertTrue()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertTrue",
         "assertTrue(a)",
         "assertThat(a).isTrue()")
   }

   fun testhandleAssertTrue_junit4()  {
      assertHandle("org.junit.Assert.assertTrue",
         "assertTrue(a)",
         "assertThat(a).isTrue()")
   }

   fun testhandleAssertTrue_withDesc()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertTrue",
         "assertTrue(a, \"desc\")",
         "assertThat(a).as(\"desc\").isTrue()")
   }

   fun testhandleAssertTrue_junit4_withDesc()  {
      assertHandle("org.junit.Assert.assertTrue",
         "assertTrue(\"desc\", a)",
         "assertThat(a).as(\"desc\").isTrue()")
   }

   fun testhandleAssertFalse()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertFalse",
         "assertFalse(a)",
         "assertThat(a).isFalse()")
   }

   fun testhandleAssertFalse_junit4()  {
      assertHandle("org.junit.Assert.assertFalse",
         "assertFalse(a)",
         "assertThat(a).isFalse()")
   }

   fun testhandleAssertFalse_withDesc()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertFalse",
         "assertFalse(a, \"desc\")",
         "assertThat(a).as(\"desc\").isFalse()")
   }

   fun testhandleAssertFalse_junit4_withDesc()  {
      assertHandle("org.junit.Assert.assertFalse",
         "assertFalse(\"desc\", a)",
         "assertThat(a).as(\"desc\").isFalse()")
   }

   fun testhandleAssertNull()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertNull",
         "assertNull(a)",
         "assertThat(a).isNull()")
   }

   fun testhandleAssertNull_junit4()  {
      assertHandle("org.junit.Assert.assertNull",
         "assertNull(a)",
         "assertThat(a).isNull()")
   }

   fun testhandleAssertNull_withDesc()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertNull",
         "assertNull(a, \"desc\")",
         "assertThat(a).as(\"desc\").isNull()")
   }

   fun testhandleAssertNull_junit4_withDesc()  {
      assertHandle("org.junit.Assert.assertNull",
         "assertNull(\"desc\", a)",
         "assertThat(a).as(\"desc\").isNull()")
   }

   fun testhandleAssertNotNull()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertNotNull",
         "assertNotNull(a)",
         "assertThat(a).isNotNull()")
   }

   fun testhandleAssertNotNull_junit4()  {
      assertHandle("org.junit.Assert.assertNotNull",
         "assertNotNull(a)",
         "assertThat(a).isNotNull()")
   }

   fun testhandleAssertNotNull_withDesc()  {
      assertHandle("org.junit.jupiter.api.Assertions.assertNotNull",
         "assertNotNull(a, \"desc\")",
         "assertThat(a).as(\"desc\").isNotNull()")
   }

   fun testhandleAssertNotNull_junit4_withDesc()  {
      assertHandle("org.junit.Assert.assertNotNull",
         "assertNotNull(\"desc\", a)",
         "assertThat(a).as(\"desc\").isNotNull()")
   }

   fun testhandleAssertThrows()  {
      assertHandle("org.junit.Assert.assertThrows",
         "assertThrows(IllegalStateExcption.class, () -> {throw new IllegalStateException()})",
         "assertThatExceptionOfType(IllegalStateExcption.class).isThrownBy(() -> {throw new IllegalStateException()})")
   }

   fun testhandleAssertThrows_withDesc()  {
      assertHandle("org.junit.Assert.assertThrows",
         "assertThrows(IllegalStateExcption.class, () -> {throw new IllegalStateException()}, \"desc\")",
         "assertThatExceptionOfType(IllegalStateExcption.class).as(\"desc\").isThrownBy(() -> {throw new IllegalStateException()})")
   }

   fun testhandleAssertFail()  {
      assertHandle("org.junit.Assert.fail",
         "fail()",
         "fail()")
   }

   fun testhandleAssertFail_withDesc()  {
      assertHandle("org.junit.Assert.fail",
         "fail(\"desc\")",
         "fail(\"desc\")")
   }

   fun testhandleAssertFail_withThrowable()  {
      assertHandle("org.junit.Assert.fail",
         "fail(new IllegalStateException())",
         "fail(new IllegalStateException())")
   }

   fun testhandleAssertFail_withThrowableAndDesc()  {
      assertHandle("org.junit.Assert.fail",
         "fail(e, \"desc\")",
         "fail(\"desc\", e)")
   }

   private fun assertCanHandle(import: String,
                               methodCall: String) =
      assertCanHandle(listOf(import), methodCall)

   private fun assertCanHandle(imports: List<String>,
                               methodCall: String) {
      val java = getJavaText(imports, methodCall)
      val myFile = myFixture.addFileToProject(module.name + "/p/" + "foo.java",
         java)
      assertCanHandle(myFile)
   }

   private fun assertCantHandle(import: String,
                                methodCall: String) =
      assertCantHandle(listOf(import), methodCall)

   private fun assertCantHandle(imports: List<String>,
                                methodCall: String) {
      val java = getJavaText(imports, methodCall)
      val myFile = myFixture.addFileToProject(module.name + "/p/" + "foo.java",
         java)
      assertCantHandle(myFile)
   }

   private fun getJavaText(import: String, methodCall: String): String {
      return getJavaText(listOf(import), methodCall)
   }

   private fun getJavaText(imports: List<String>, methodCall: String): String {
      return imports.joinToString("\n") {import -> "import static $import;"} +
         "\n\n" +
         "class foo {\n" +
         "    @Test\n" +
         "    void foo() {\n" +
         "        $methodCall;\n" +
         "    } \n" +
         "}    "
   }

   private fun assertCanHandle(myFile: PsiFile) {
      assertCanHandle(myFile, true)
   }

   private fun assertCantHandle(myFile: PsiFile) {
      assertCanHandle(myFile, false)
   }

   private fun assertCanHandle(myFile: PsiFile,
                               canHandle: Boolean) {
      val psiMethodCallExpression = TestUtil.getPsiMethodCallExpression(myFile)

      ReadAction.run<IllegalStateException> {
         if (psiMethodCallExpression == null)
            Assertions.fail("Missing method call")
         else
            Assertions.assertThat(JunitHandler().canHandle(psiMethodCallExpression)).isEqualTo(canHandle)
      }
   }

   private fun assertHandle(import: String,
                            methodCall: String,
                            updatedMethodCall: String) {
      val java = getJavaText(import, methodCall)
      val myFile = myFixture.addFileToProject(module.name + "/p/" + "foo.java",
         java)
      assertHandle(myFile, updatedMethodCall)
   }

   private fun assertHandle(myFile: PsiFile,
                            expected: String) {
      TestUtil.assertHandle(JunitHandler(), project, myFile, expected)
   }
}