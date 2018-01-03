package com.chainstaysoftware.testing

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase
import com.intellij.util.PathUtil
import org.assertj.core.api.Assertions
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File


class HamcrestHandlerTest : JavaCodeInsightFixtureTestCase() {

   @BeforeEach
   fun setup() {
      super.setUp()

      val pathForJunit4 = PathUtil.getJarPathForClass(Assert::class.java)
      PsiTestUtil.addLibrary(myModule, "junit4", StringUtil.getPackageName(pathForJunit4, File.separatorChar),
         StringUtil.getShortName(pathForJunit4, File.separatorChar))

      val pathForHamcrest = PathUtil.getJarPathForClass(MatcherAssert::class.java)
      PsiTestUtil.addLibrary(myModule, "hamcrest", StringUtil.getPackageName(pathForHamcrest, File.separatorChar),
         StringUtil.getShortName(pathForHamcrest, File.separatorChar))
   }

   @AfterEach
   fun cleanup() {
      super.tearDown()
   }

   override fun getName(): String {
      return "HamcrestHandlerTest"
   }

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
   fun handleMatcherAssertEqualTo()  {
      assertHandle("org.hamcrest.MatcherAssert.assertThat",
         "assertThat(\"foo\", 2, equalTo(2))",
         "Assertions.assertThat(2).as(\"foo\").isEqualTo(2)")
   }

   private fun assertCanHandle(import: String,
                               methodCall: String) {
      val java = getJavaText(import, methodCall)
      val myFile = myFixture.addFileToProject(myModule.name + "/p/" + "foo.java",
         java)
      assertCanHandle(myFile)
   }

   private fun assertCantHandle(import: String,
                               methodCall: String) {
      val java = getJavaText(import, methodCall)
      val myFile = myFixture.addFileToProject(myModule.name + "/p/" + "foo.java",
         java)
      assertCantHandle(myFile)
   }

   private fun getJavaText(import: String, methodCall: String): String {
      return "import static $import;\n" +
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