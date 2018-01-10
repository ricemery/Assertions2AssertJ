package com.chainstaysoftware.testing

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase
import com.intellij.util.PathUtil
import org.assertj.core.api.Assertions
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File


// TODO: Fix tests for MatcherAssert - there is a class path issue
// TODO: Add more tests that show recursive matchers are not supported
class HamcrestHandlerTest : JavaCodeInsightFixtureTestCase() {

   @BeforeEach
   fun setup() {
      super.setUp()

      val pathForJunit4 = PathUtil.getJarPathForClass(Assert::class.java)
      PsiTestUtil.addLibrary(myModule, "junit4", StringUtil.getPackageName(pathForJunit4, File.separatorChar),
         StringUtil.getShortName(pathForJunit4, File.separatorChar))

      val pathForHamcrestCore = PathUtil.getJarPathForClass(MatcherAssert::class.java)
      PsiTestUtil.addLibrary(myModule, "hamcrest-core", pathForHamcrestCore)

      val pathForHamcrestAll = PathUtil.getJarPathForClass(Matchers::class.java)
      PsiTestUtil.addLibrary(myModule, "hamcrest-all", pathForHamcrestAll)
   }

   @AfterEach
   fun cleanup() {
      super.tearDown()
   }

   override fun getName(): String {
      return "HamcrestHandlerTest"
   }

   @Disabled // Something screwy with classpath
   @Test
   fun canHandleMatcherAssert()  {
      assertCanHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, equalTo(2))")
   }

   @Test
   fun canHandleAssert()  {
      assertCanHandle("org.junit.Assert.assertThat",
         "assertThat(1, equalTo(2))")
   }

   @Test
   fun cantHandleAssert()  {
      assertCantHandle("org.junit.Assert.assertEquals",
         "assertEquals(1, 2)")
   }

   @Test
   fun canHandleAssert_is_equalTo()  {
      assertCanHandle(listOf("org.junit.Assert.assertThat",
                              "org.hamcrest.CoreMatchers.*"),
         "assertThat(1, is(equalTo(2)))")
   }

   @Test
   fun canHandleAssert_not_equalTo()  {
      assertCanHandle(listOf("org.junit.Assert.assertThat",
         "org.hamcrest.CoreMatchers.*"),
         "assertThat(1, not(equalTo(2)))")
   }

   @Test
   fun canHandleAssert_is_emptyArray()  {
      assertCanHandle(listOf("org.junit.Assert.assertThat",
         "org.hamcrest.CoreMatchers.*"),
         "assertThat(new int[]{1}, is(emptyArray()))")
   }

   @Test
   fun canHandleAssert_not_emptyArray()  {
      assertCanHandle(listOf("org.junit.Assert.assertThat",
         "org.hamcrest.CoreMatchers.*"),
         "assertThat(new int[]{1}, not(emptyArray()))")
   }

   @Test
   fun canHandleAssert_is_emptyIterable()  {
      assertCanHandle(listOf("org.junit.Assert.assertThat",
         "org.hamcrest.CoreMatchers.*"),
         "assertThat(new List(), is(emptyIterable()))")
   }

   @Test
   fun canHandleAssert_not_emptyIterable()  {
      assertCanHandle(listOf("org.junit.Assert.assertThat",
         "org.hamcrest.CoreMatchers.*"),
         "assertThat(new List(), not(emptyIterable()))")
   }

   @Disabled // Something screwy with classpath
   @Test
   fun cantHandleAssert_anyOf()  {
      assertCantHandle(listOf("org.junit.Assert.assertThat",
         "org.hamcrest.Matchers.*"),
         "assertThat(new List(), anyOf(is(nullValue()), is(not(nullValue()))")
   }

   @Test
   fun handleMatcherAssert_equalToTrue()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", true, equalTo(true))",
         "Assertions.assertThat(true).as(\"foo\").isTrue()")
   }

   @Test
   fun handleMatcherAssert_equalToFalse()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", false, equalTo(false))",
         "Assertions.assertThat(false).as(\"foo\").isFalse()")
   }

   @Test
   fun handleMatcherAssert_isTrue()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", true, is(true))",
         "Assertions.assertThat(true).as(\"foo\").isTrue()")
   }

   @Test
   fun handleMatcherAssert_isFalse()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", false, is(false))",
         "Assertions.assertThat(false).as(\"foo\").isFalse()")
   }

   @Test
   fun handleMatcherAssert_notTrue()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", true, not(true))",
         "Assertions.assertThat(true).as(\"foo\").isFalse()")
   }

   @Test
   fun handleMatcherAssert_notFalse()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", true, not(false))",
         "Assertions.assertThat(true).as(\"foo\").isTrue()")
   }

   @Test
   fun handleMatcherAssert_notEmptyArray()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", new byte[]{}, not(emptyArray()))",
         "Assertions.assertThat(new byte[]{}).as(\"foo\").isNotEmpty()")
   }

   @Test
   fun handleMatcherAssert_notEmptyIterable()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", new byte[]{}, not(emptyIterable()))",
         "Assertions.assertThat(new byte[]{}).as(\"foo\").isNotEmpty()")
   }

   @Test
   fun handleMatcherAssert_equalTo()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, equalTo(2))",
         "Assertions.assertThat(2).as(\"foo\").isEqualTo(2)")
   }

   @Test
   fun handleMatcherAssert_equalToIgnoreCase()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", \"fOObar\", equalToIgnoringCase(\"foobar\"))",
         "Assertions.assertThat(\"fOObar\").as(\"foo\").isEqualToIgnoringCase(\"foobar\")")
   }

   @Test
   fun handleMatcherAssert_equalToIgnoreWhitespace()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", \"fOO\tbar\", equalToIgnoringWhiteSpace(\"foobar\"))",
         "Assertions.assertThat(\"fOO\tbar\").as(\"foo\").isEqualToIgnoringWhitespace(\"foobar\")")
   }

   @Test
   fun handleMatcherAssert_closeTo()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2.0, closeTo(2.0, 0.0001))",
         "Assertions.assertThat(2.0).as(\"foo\").isCloseTo(2.0, Assertions.offset(0.0001))")
   }

   @Test
   fun handleMatcherAssert_hasItems()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", Arrays.asList(\"foo\", \"bar\", \"baz\"), hasItems(\"baz\", \"foo\"))",
         "Assertions.assertThat(Arrays.asList(\"foo\", \"bar\", \"baz\"))" +
            ".as(\"desc\").contains(\"baz\", \"foo\")")
   }

   @Test
   fun handleMatcherAssert_hasItem()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", Arrays.asList(\"foo\", \"bar\", \"baz\"), hasItem(\"baz\"))",
         "Assertions.assertThat(Arrays.asList(\"foo\", \"bar\", \"baz\"))" +
            ".as(\"desc\").contains(\"baz\")")
   }

   @Test
   fun handleMatcherAssert_hasEntry()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", myMap, hasEntry(\"bar\", \"foo\"))",
         "Assertions.assertThat(myMap)" +
            ".as(\"desc\").containsKey(\"bar\", \"foo\")")
   }

   @Test
   fun handleMatcherAssert_containsString()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", \"myStringOfNote\", containsString(\"ring\"))",
         "Assertions.assertThat(\"myStringOfNote\")" +
            ".as(\"desc\").contains(\"ring\")")
   }

   @Test
   fun handleMatcherAssert_is()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"is a cheese instance\", cheese, is(Cheddar.class))",
         "Assertions.assertThat(cheese)" +
            ".as(\"is a cheese instance\").isInstanceOf(Cheddar.class)")
   }

   @Test
   fun handleMatcherAssert_isEmptyString()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", b, isEmptyString())",
         "Assertions.assertThat(b)" +
            ".as(\"desc\").isEmpty()")
   }

   @Test
   fun handleMatcherAssert_notIsEmptyString()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", b, not(isEmptyString()))",
         "Assertions.assertThat(b)" +
            ".as(\"desc\").isNotEmpty()")
   }

   @Test
   fun handleMatcherAssert_isEmptyOrNullString()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", b, isEmptyOrNullString())",
         "Assertions.assertThat(b)" +
            ".as(\"desc\").isBlank()")
   }

   @Test
   fun handleMatcherAssert_notIsEmptyOrNullString()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", b, not(isEmptyOrNullString()))",
         "Assertions.assertThat(b)" +
            ".as(\"desc\").isNotBlank()")
   }

   @Test
   fun handleMatcherAssert_notNullValue()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", a, notNullValue())",
         "Assertions.assertThat(a)" +
            ".as(\"desc\").isNotNull()")
   }

   @Test
   fun handleMatcherAssert_nullValue()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", a, nullValue())",
         "Assertions.assertThat(a)" +
            ".as(\"desc\").isNull()")
   }

   @Test
   fun handleMatcherAssert_instanceOf()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"is a cheese instance\", cheese, instanceOf(Cheddar.class))",
         "Assertions.assertThat(cheese)" +
            ".as(\"is a cheese instance\").isInstanceOf(Cheddar.class)")
   }

   @Test
   fun handleMatcherAssert_any()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"is a cheese instance\", cheese, any(Cheddar.class))",
         "Assertions.assertThat(cheese)" +
            ".as(\"is a cheese instance\").isInstanceOf(Cheddar.class)")
   }

   @Test
   fun handleMatcherAssert_lessThan()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, lessThan(3))",
         "Assertions.assertThat(2)" +
            ".as(\"foo\").isLessThan(3)")
   }

   @Test
   fun handleMatcherAssert_lessThanOrEqualTo()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, lessThanOrEqualTo(3))",
         "Assertions.assertThat(2)" +
            ".as(\"foo\").isLessThanOrEqualTo(3)")
   }

   @Test
   fun handleMatcherAssert_lessThan_Instant()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", Instant.now(), lessThan(Instant.now()))",
         "Assertions.assertThat(Instant.now())" +
            ".as(\"foo\").isBefore(Instant.now())")
   }

   @Test
   fun handleMatcherAssert_lessThanOrEqualTo_Instant()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", Instant.now(), lessThanOrEqualTo(Instant.now()))",
         "Assertions.assertThat(Instant.now())" +
            ".as(\"foo\").isBeforeOrEqualTo(Instant.now())")
   }

   @Test
   fun handleMatcherAssert_greaterThan()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, greaterThan(3))",
         "Assertions.assertThat(2)" +
            ".as(\"foo\").isGreaterThan(3)")
   }

   @Test
   fun handleMatcherAssert_greaterThanOrEqualTo()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, greaterThanOrEqualTo(3))",
         "Assertions.assertThat(2)" +
            ".as(\"foo\").isGreaterThanOrEqualTo(3)")
   }

   @Test
   fun handleMatcherAssert_greaterThan_Instant()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", Instant.now(), greaterThan(Instant.now()))",
         "Assertions.assertThat(Instant.now())" +
            ".as(\"foo\").isAfter(Instant.now())")
   }

   @Test
   fun handleMatcherAssert_greaterThanOrEqualTo_Instant()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", Instant.now(), greaterThanOrEqualTo(Instant.now()))",
         "Assertions.assertThat(Instant.now())" +
            ".as(\"foo\").isAfterOrEqualTo(Instant.now())")
   }

   @Test
   fun handleMatcherAssert_contains()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", Arrays.asList(\"foo\", \"bar\"), " +
            "contains(\"foo\", \"bar\"))",
         "Assertions.assertThat(Arrays.asList(\"foo\", \"bar\"))" +
            ".as(\"foo\").containsExactly(\"foo\", \"bar\")")
   }

   @Test
   fun handleMatcherAssert_containsInAnyOrder()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", Arrays.asList(\"foo\", \"bar\"), " +
            "containsInAnyOrder(\"foo\", \"bar\"))",
         "Assertions.assertThat(Arrays.asList(\"foo\", \"bar\"))" +
            ".as(\"foo\").containsAll(\"foo\", \"bar\")")
   }

   @Test
   fun handleMatcherAssert_sameInstance()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"is the same instance\", a, sameInstance(b))",
         "Assertions.assertThat(a)" +
            ".as(\"is the same instance\").isSameAs(b)")
   }

   @Test
   fun handleMatcherAssert_theInstance()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"is the same instance\", a, theInstance(b))",
         "Assertions.assertThat(a)" +
            ".as(\"is the same instance\").isSameAs(b)")
   }

   @Test
   fun handleMatcherAssert_startsWith()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", \"fOObar\", startsWith(\"fOO\"))",
         "Assertions.assertThat(\"fOObar\").as(\"desc\").startsWith(\"fOO\")")
   }

   @Test
   fun handleMatcherAssert_endsWith()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", \"fOObar\", endsWith(\"bar\"))",
         "Assertions.assertThat(\"fOObar\").as(\"desc\").endsWith(\"bar\")")
   }

   @Test
   fun handleMatcherAssert_allOf()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", \"foobar\", allOf(startsWith(\"foo\"), containsString(\"or\")))",
         "Assertions.assertThat(\"foobar\").as(\"desc\").startsWith(\"foo\").contains(\"or\")")
   }

   @Test
   fun arrayContainingInAnyOrder()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", new String[]{\"foo\", \"bar\"}, arrayContaining(\"foo\", \"bar\"))",
         "Assertions.assertThat(new String[]{\"foo\", \"bar\"}).as(\"desc\").containsExactly(\"foo\", \"bar\")")
   }

   @Test
   fun handleMatcherAssert_arrayContaining()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", new String[]{\"foo\", \"bar\"}, arrayContainingInAnyOrder(\"foo\", \"bar\"))",
         "Assertions.assertThat(new String[]{\"foo\", \"bar\"}).as(\"desc\").contains(\"foo\", \"bar\")")
   }

   @Test
   fun handleMatcherAssert_arrayWithSize()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", new String[]{\"foo\", \"bar\"}, arrayWithSize(2))",
         "Assertions.assertThat(new String[]{\"foo\", \"bar\"}).as(\"desc\").hasSize(2)")
   }

   @Test
   fun handleMatcherAssert_emptyArray()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", new String[]{}, emptyArray())",
         "Assertions.assertThat(new String[]{}).as(\"desc\").isEmpty()")
   }

   @Test
   fun handleMatcherAssert_emptyIterable()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", new String[]{}, emptyIterable())",
         "Assertions.assertThat(new String[]{}).as(\"desc\").isEmpty()")
   }

   private fun assertCanHandle(import: String,
                               methodCall: String) =
      assertCanHandle(listOf(import), methodCall)

   private fun assertCanHandle(imports: List<String>,
                               methodCall: String) {
      val java = getJavaText(imports, methodCall)
      val myFile = myFixture.addFileToProject(myModule.name + "/p/" + "foo.java",
         java)
      assertCanHandle(myFile)
   }

   private fun assertCantHandle(import: String,
                                methodCall: String) =
      assertCantHandle(listOf(import), methodCall)

   private fun assertCantHandle(imports: List<String>,
                                methodCall: String) {
      val java = getJavaText(imports, methodCall)
      val myFile = myFixture.addFileToProject(myModule.name + "/p/" + "foo.java",
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
            Assertions.assertThat(HamcrestHandler().canHandle(psiMethodCallExpression)).isEqualTo(canHandle)
      }
   }

   private fun assertHandle(import: String,
                            methodCall: String,
                            updatedMethodCall: String) {
      val java = getJavaText(import, methodCall)
      val myFile = myFixture.addFileToProject(myModule.name + "/p/" + "foo.java",
         java)
      assertHandle(myFile, updatedMethodCall)
   }

   private fun assertHandle(myFile: PsiFile,
                            expected: String) {
      TestUtil.assertHandle(HamcrestHandler(), project, myFile, expected)
   }
}