package com.chainstaysoftware.testing

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase
import com.intellij.util.PathUtil
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.MatcherAssert
import org.junit.Assert
import java.io.File


// TODO: Add more tests that show recursive matchers are mostly not supported
class HamcrestHandlerTest : JavaCodeInsightFixtureTestCase() {

   override fun setUp() {
      super.setUp()

      val pathForJunit4 = PathUtil.getJarPathForClass(Assert::class.java)
      PsiTestUtil.addLibrary(module, "junit4", StringUtil.getPackageName(pathForJunit4, File.separatorChar),
         StringUtil.getShortName(pathForJunit4, File.separatorChar))
      PsiTestUtil.addLibrary(
         module,
         "hamcrest",
         PathUtil.getJarPathForClass(MatcherAssert::class.java)
      )
   }

   fun testCanHandleMatcherAssert()  {
      assertCanHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, equalTo(2))")
   }

   fun testcanHandleAssert()  {
      assertCanHandle("org.junit.Assert.assertThat",
         "assertThat(1, equalTo(2))")
   }

   fun testcantHandleAssert()  {
      assertCantHandle("org.junit.Assert.assertEquals",
         "assertEquals(1, 2)")
   }

   fun testcanHandleAssert_is_equalTo()  {
      assertCanHandle(listOf("org.junit.Assert.assertThat",
         "org.hamcrest.CoreMatchers.*"),
         "assertThat(1, is(equalTo(2)))")
   }

   fun testhandleMatcherAssert_comparesEqualTo()  {
      assertHandle("org.junit.Assert.assertThat",
         "assertThat(1, comparesEqualTo(1.0))",
         "assertThat(1).isEqualByComparingTo(1.0)")
   }

   fun testhandleAssert_not_equalTo()  {
      assertHandle("org.junit.Assert.assertThat",
         "assertThat(1, not(equalTo(2)))",
         "assertThat(1).isNotEqualTo(2)")
   }

   fun testhandleAssert_not_is()  {
      assertHandle("org.junit.Assert.assertThat",
         "assertThat(1, not(is(2)))",
         "assertThat(1).isNotEqualTo(2)")
   }

   fun testhandleAssert_is_not()  {
      assertHandle("org.junit.Assert.assertThat",
         "assertThat(1, is(not(2)))",
         "assertThat(1).isNotEqualTo(2)")
   }

   fun testcanHandleAssert_is_empty()  {
      assertCanHandle(listOf("org.junit.Assert.assertThat",
         "org.hamcrest.Matchers.*"),
         "assertThat(new List(), is(empty())")
   }

   fun testcanHandleAssert_is_emptyArray()  {
      assertCanHandle(listOf("org.junit.Assert.assertThat",
         "org.hamcrest.CoreMatchers.*"),
         "assertThat(new int[]{1}, is(emptyArray()))")
   }

   fun testcanHandleAssert_not_emptyArray()  {
      assertCanHandle(listOf("org.junit.Assert.assertThat",
         "org.hamcrest.CoreMatchers.*"),
         "assertThat(new int[]{1}, not(emptyArray()))")
   }

   fun testcanHandleAssert_is_emptyIterable()  {
      assertCanHandle(listOf("org.junit.Assert.assertThat",
         "org.hamcrest.CoreMatchers.*"),
         "assertThat(new List(), is(emptyIterable()))")
   }

   fun testcanHandleAssert_not_emptyIterable()  {
      assertCanHandle(listOf("org.junit.Assert.assertThat",
         "org.hamcrest.CoreMatchers.*"),
         "assertThat(new List(), not(emptyIterable()))")
   }

   fun testcantHandleAssert_allOf()  {
      assertCanHandle(listOf("org.junit.Assert.assertThat",
         "org.hamcrest.Matchers.*"),
         "assertThat(new List(), allOf(is(nullValue()), is(nullValue())")
   }

   fun testcantHandleAssert_bothOf()  {
      assertCanHandle(listOf("org.junit.Assert.assertThat",
         "org.hamcrest.Matchers.*"),
         "assertThat(new List(), allOf(is(nullValue()), is(nullValue()))")
   }

   fun testcantHandleAssert_anyOf()  {
       assertCanHandle(listOf("org.junit.Assert.assertThat", "org.hamcrest.Matchers.*"),
         "assertThat(new List(), anyOf(is(nullValue()), is(not(nullValue()))")
   }

   fun testhandleMatcherAssert_equalToTrue()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", true, equalTo(true))",
         "assertThat(true).as(\"foo\").isTrue()")
   }

   fun testhandleMatcherAssert_equalToFalse()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", false, equalTo(false))",
         "assertThat(false).as(\"foo\").isFalse()")
   }

   fun testhandleMatcherAssert_isTrue()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", true, is(true))",
         "assertThat(true).as(\"foo\").isTrue()")
   }

   fun testhandleMatcherAssert_isFalse()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", false, is(false))",
         "assertThat(false).as(\"foo\").isFalse()")
   }

   fun testhandleMatcherAssert_notTrue()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", true, not(true))",
         "assertThat(true).as(\"foo\").isFalse()")
   }

   fun testhandleMatcherAssert_notFalse()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", true, not(false))",
         "assertThat(true).as(\"foo\").isTrue()")
   }

   fun testhandleMatcherAssert_notEmptyArray()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", new byte[]{}, not(emptyArray()))",
         "assertThat(new byte[]{}).as(\"foo\").isNotEmpty()")
   }

   fun testhandleMatcherAssert_notEmptyIterable()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", new byte[]{}, not(emptyIterable()))",
         "assertThat(new byte[]{}).as(\"foo\").isNotEmpty()")
   }

   fun testhandleMatcherAssert_equalTo()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, equalTo(2))",
         "assertThat(2).as(\"foo\").isEqualTo(2)")
   }

   fun testhandleMatcherAssert_equalToIgnoreCase()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", \"fOObar\", equalToIgnoringCase(\"foobar\"))",
         "assertThat(\"fOObar\").as(\"foo\").isEqualToIgnoringCase(\"foobar\")")
   }

   fun testhandleMatcherAssert_equalToIgnoreWhitespace()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", \"fOO\tbar\", equalToIgnoringWhiteSpace(\"foobar\"))",
         "assertThat(\"fOO\tbar\").as(\"foo\").isEqualToIgnoringWhitespace(\"foobar\")")
   }

   fun testhandleMatcherAssert_closeTo()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2.0, closeTo(2.0, 0.0001))",
         "assertThat(2.0).as(\"foo\").isCloseTo(2.0, offset(0.0001))")
   }

   fun testhandleMatcherAssert_hasItems()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", Arrays.asList(\"foo\", \"bar\", \"baz\"), hasItems(\"baz\", \"foo\"))",
         "assertThat(Arrays.asList(\"foo\", \"bar\", \"baz\"))" +
            ".as(\"desc\").contains(\"baz\", \"foo\")")
   }

   fun testhandleMatcherAssert_hasItem()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", Arrays.asList(\"foo\", \"bar\", \"baz\"), hasItem(\"baz\"))",
         "assertThat(Arrays.asList(\"foo\", \"bar\", \"baz\"))" +
            ".as(\"desc\").contains(\"baz\")")
   }

   fun testhandleMatcherAssert_hasEntry()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", myMap, hasEntry(\"bar\", \"foo\"))",
         "assertThat(myMap)" +
            ".as(\"desc\").containsEntry(\"bar\", \"foo\")")
   }

   fun testhandleMatcherAssert_hasKey()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", myMap, hasKey(\"bar\"))",
         "assertThat(myMap)" +
            ".as(\"desc\").containsKey(\"bar\")")
   }

   fun testhandleMatcherAssert_hasValue()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", myMap, hasValue(\"bar\"))",
         "assertThat(myMap)" +
            ".as(\"desc\").containsValue(\"bar\")")
   }

   fun testhandleMatcherAssert_hasToString()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", a, hasToString())",
         "assertThat(a)" +
            ".as(\"desc\").hasToString()")
   }

   fun testhandleMatcherAssert_containsString()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", \"myStringOfNote\", containsString(\"ring\"))",
         "assertThat(\"myStringOfNote\")" +
            ".as(\"desc\").contains(\"ring\")")
   }

   fun testhandleMatcherAssert_is()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"is a cheese instance\", cheese, is(Cheddar.class))",
         "assertThat(cheese)" +
            ".as(\"is a cheese instance\").isInstanceOf(Cheddar.class)")
   }

   fun testhandleMatcherAssert_isEmptyString()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", b, isEmptyString())",
         "assertThat(b)" +
            ".as(\"desc\").isEmpty()")
   }

   fun testhandleMatcherAssert_notIsEmptyString()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", b, not(isEmptyString()))",
         "assertThat(b)" +
            ".as(\"desc\").isNotEmpty()")
   }

   fun testhandleMatcherAssert_isEmptyOrNullString()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", b, isEmptyOrNullString())",
         "assertThat(b)" +
            ".as(\"desc\").isBlank()")
   }

   fun testhandleMatcherAssert_notIsEmptyOrNullString()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", b, not(isEmptyOrNullString()))",
         "assertThat(b)" +
            ".as(\"desc\").isNotBlank()")
   }

   fun testhandleMatcherAssert_notNullValue()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", a, notNullValue())",
         "assertThat(a)" +
            ".as(\"desc\").isNotNull()")
   }

   fun testhandleMatcherAssert_nullValue()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", a, nullValue())",
         "assertThat(a)" +
            ".as(\"desc\").isNull()")
   }

   fun testhandleMatcherAssert_is_nullValue()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", a, is(nullValue()))",
         "assertThat(a)" +
            ".as(\"desc\").isNull()")
   }

   fun testhandleMatcherAssert_instanceOf()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"is a cheese instance\", cheese, instanceOf(Cheddar.class))",
         "assertThat(cheese)" +
            ".as(\"is a cheese instance\").isInstanceOf(Cheddar.class)")
   }

   fun testhandleMatcherAssert_is_instanceOf()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"is a cheese instance\", cheese, is(instanceOf(Cheddar.class)))",
         "assertThat(cheese)" +
            ".as(\"is a cheese instance\").isInstanceOf(Cheddar.class)")
   }

   fun testhandleMatcherAssert_typeCompatibleWith()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"is a cheese instance\", cheese, typeCompatibleWith(Cheddar.class))",
         "assertThat(cheese)" +
            ".as(\"is a cheese instance\").isInstanceOf(Cheddar.class)")
   }

   fun testhandleMatcherAssert_any()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"is a cheese instance\", cheese, any(Cheddar.class))",
         "assertThat(cheese)" +
            ".as(\"is a cheese instance\").isInstanceOf(Cheddar.class)")
   }

   fun testhandleMatcherAssert_is_any()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"is a cheese instance\", cheese, is(any(Cheddar.class)))",
         "assertThat(cheese)" +
            ".as(\"is a cheese instance\").isInstanceOf(Cheddar.class)")
   }

   fun testhandleMatcherAssert_lessThan()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, lessThan(3))",
         "assertThat(2)" +
            ".as(\"foo\").isLessThan(3)")
   }

   fun testhandleMatcherAssert_is_lessThan()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, is(lessThan(3)))",
         "assertThat(2)" +
            ".as(\"foo\").isLessThan(3)")
   }

   fun testhandleMatcherAssert_lessThanOrEqualTo()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, lessThanOrEqualTo(3))",
         "assertThat(2)" +
            ".as(\"foo\").isLessThanOrEqualTo(3)")
   }

   fun testhandleMatcherAssert_is_lessThanOrEqualTo()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, is(lessThanOrEqualTo(3)))",
         "assertThat(2)" +
            ".as(\"foo\").isLessThanOrEqualTo(3)")
   }

   fun testhandleMatcherAssert_lessThan_Instant()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", Instant.now(), lessThan(Instant.now()))",
         "assertThat(Instant.now())" +
            ".as(\"foo\").isBefore(Instant.now())")
   }

   fun testhandleMatcherAssert_lessThanOrEqualTo_Instant()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", Instant.now(), lessThanOrEqualTo(Instant.now()))",
         "assertThat(Instant.now())" +
            ".as(\"foo\").isBeforeOrEqualTo(Instant.now())")
   }

   fun testhandleMatcherAssert_greaterThan()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, greaterThan(3))",
         "assertThat(2)" +
            ".as(\"foo\").isGreaterThan(3)")
   }

   fun testhandleMatcherAssert_is_greaterThan()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, is(greaterThan(3)))",
         "assertThat(2)" +
            ".as(\"foo\").isGreaterThan(3)")
   }

   fun testhandleMatcherAssert_greaterThanOrEqualTo()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, greaterThanOrEqualTo(3))",
         "assertThat(2)" +
            ".as(\"foo\").isGreaterThanOrEqualTo(3)")
   }

   fun testhandleMatcherAssert_is_greaterThanOrEqualTo()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, is(greaterThanOrEqualTo(3)))",
         "assertThat(2)" +
            ".as(\"foo\").isGreaterThanOrEqualTo(3)")
   }


   fun testhandleMatcherAssert_greaterThan_Instant()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", Instant.now(), greaterThan(Instant.now()))",
         "assertThat(Instant.now())" +
            ".as(\"foo\").isAfter(Instant.now())")
   }

   fun testhandleMatcherAssert_greaterThanOrEqualTo_Instant()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", Instant.now(), greaterThanOrEqualTo(Instant.now()))",
         "assertThat(Instant.now())" +
            ".as(\"foo\").isAfterOrEqualTo(Instant.now())")
   }

   fun testhandleMatcherAssert_contains()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", Arrays.asList(\"foo\", \"bar\"), " +
            "contains(\"foo\", \"bar\"))",
         "assertThat(Arrays.asList(\"foo\", \"bar\"))" +
            ".as(\"foo\").containsExactly(\"foo\", \"bar\")")
   }

   fun testhandleMatcherAssert_containsInAnyOrder()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", Arrays.asList(\"foo\", \"bar\"), " +
            "containsInAnyOrder(\"foo\", \"bar\"))",
         "assertThat(Arrays.asList(\"foo\", \"bar\"))" +
            ".as(\"foo\").contains(\"foo\", \"bar\")")
   }

   fun testhandleMatcherAssert_sameInstance()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"is the same instance\", a, sameInstance(b))",
         "assertThat(a)" +
            ".as(\"is the same instance\").isSameAs(b)")
   }

   fun testhandleMatcherAssert_theInstance()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"is the same instance\", a, theInstance(b))",
         "assertThat(a)" +
            ".as(\"is the same instance\").isSameAs(b)")
   }

   fun testhandleMatcherAssert_startsWith()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", \"fOObar\", startsWith(\"fOO\"))",
         "assertThat(\"fOObar\").as(\"desc\").startsWith(\"fOO\")")
   }

   fun testhandleMatcherAssert_endsWith()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", \"fOObar\", endsWith(\"bar\"))",
         "assertThat(\"fOObar\").as(\"desc\").endsWith(\"bar\")")
   }

   fun testhandleMatcherAssert_allOf()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", \"foobar\", allOf(startsWith(\"foo\"), containsString(\"or\")))",
         "assertThat(\"foobar\").as(\"desc\").startsWith(\"foo\").contains(\"or\")")
   }

   fun testhandleMatcherAssert_both()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", \"foobar\", both(startsWith(\"foo\"), containsString(\"or\")))",
         "assertThat(\"foobar\").as(\"desc\").startsWith(\"foo\").contains(\"or\")")
   }

   fun testarrayContainingInAnyOrder()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", new String[]{\"foo\", \"bar\"}, arrayContaining(\"foo\", \"bar\"))",
         "assertThat(new String[]{\"foo\", \"bar\"}).as(\"desc\").containsExactly(\"foo\", \"bar\")")
   }

   fun testhandleMatcherAssert_arrayContaining()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", new String[]{\"foo\", \"bar\"}, arrayContainingInAnyOrder(\"foo\", \"bar\"))",
         "assertThat(new String[]{\"foo\", \"bar\"}).as(\"desc\").contains(\"foo\", \"bar\")")
   }

   fun testhandleMatcherAssert_arrayWithSize()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", new String[]{\"foo\", \"bar\"}, arrayWithSize(2))",
         "assertThat(new String[]{\"foo\", \"bar\"}).as(\"desc\").hasSize(2)")
   }

   fun testhandleMatcherAssert_hasSize()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", a, hasSize(2))",
         "assertThat(a).as(\"desc\").hasSize(2)")
   }

   fun testhandleMatcherAssert_iterableWithSize()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", a, iterableWithSize(2))",
         "assertThat(a).as(\"desc\").hasSize(2)")
   }

   fun testhandleMatcherAssert_empty()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", new List(), empty())",
         "assertThat(new List()).as(\"desc\").isEmpty()")
   }

   fun testhandleMatcherAssert_is_empty()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", new List(), is(empty()))",
         "assertThat(new List()).as(\"desc\").isEmpty()")
   }

   fun testhandleMatcherAssert_not_empty()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", new List(), not(empty()))",
         "assertThat(new List()).as(\"desc\").isNotEmpty()")
   }

   fun testhandleMatcherAssert_emptyArray()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", new String[]{}, emptyArray())",
         "assertThat(new String[]{}).as(\"desc\").isEmpty()")
   }

   fun testhandleMatcherAssert_emptyIterable()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", new String[]{}, emptyIterable())",
         "assertThat(new String[]{}).as(\"desc\").isEmpty()")
   }

   fun testhandleMatcherAssert_emptyCollectionOf()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", new List(), emptyCollectionOf())",
         "assertThat(new List()).as(\"desc\").isEmpty()")
   }

   fun testhandleMatcherAssert_is_emptyCollectionOf()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"desc\", new List(), is(emptyCollectionOf()))",
         "assertThat(new List()).as(\"desc\").isEmpty()")
   }

   fun testhandleMatcherAssert_describedAs()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(a, describedAs(\"some desc params - %0 %1\", equalTo(a), \"param1\", \"param2\"))",
         "assertThat(a).as(\"some desc params - %0 %1\", \"param1\", \"param2\").isEqualTo(a)")
   }

   fun testassertThat_description() {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"description\", condition)",
         "assertThat(condition).as(\"description\").isTrue()")
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
      return imports.joinToString("\n") { import -> "import static $import;" } +
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
            fail("Missing method call")
         else
            assertThat(HamcrestHandler().canHandle(psiMethodCallExpression)).isEqualTo(canHandle)
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
      TestUtil.assertHandle(HamcrestHandler(), project, myFile, expected)
   }
}
