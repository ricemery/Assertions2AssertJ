package com.chainstaysoftware.testing

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Computable
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiImportList
import com.intellij.psi.PsiImportStatement
import com.intellij.psi.PsiImportStaticStatement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.siyeh.ig.psiutils.MethodCallUtils
import java.util.ArrayList


object Util {
   /**
    * Traverses a Project looking for files in the test scope.
    * Running the consumer on each file. A GlobalSearchScope can be
    * passed in to limit the scope of the traverse. If no GlobalSearchScope
    * is passed in, then the Project Scope is used.
    */
   fun traverseTestFiles(project: Project,
                         consumer: (PsiFile) -> Unit,
                         globalSearchScope: GlobalSearchScope? = null) {
      val projectFileIndex = ProjectFileIndex.SERVICE.getInstance(project)
      val searchScope = globalSearchScope ?: GlobalSearchScope.projectScope(project)

      val files = ApplicationManager.getApplication().runReadAction(Computable {
         FileBasedIndex.getInstance().getContainingFiles(
            FileTypeIndex.NAME,
            JavaFileType.INSTANCE,
            searchScope)
            .filter { virtualFile -> projectFileIndex.isInTestSourceContent(virtualFile) }
            .map { virtualFile -> PsiManager.getInstance(project).findFile(virtualFile) }
      })

      files.forEach{ psiFile -> if (psiFile != null) consumer(psiFile) }
   }

   /**
    * Determines if qualifiedName is in the classPath of the Project
    */
   fun inClasspath(project: Project, qualifiedName: String) =
      JavaPsiFacade.getInstance(project).findClass(qualifiedName, GlobalSearchScope.allScope(project)) != null

   /**
    * Adds an import to the passed in psiFile.
    */
   fun addImport(project: Project, psiFile: PsiFile, qualifiedName: String) {
      val layoutInflaterPsiClass = JavaPsiFacade.getInstance(project).findClass(qualifiedName, GlobalSearchScope.allScope(project))
      val psiImportList = findElement(psiFile, PsiImportList::class.java)

      if (psiImportList != null) {
         if (psiImportList.children.any { it is PsiImportStatement && it.qualifiedName == qualifiedName }) {
            // we already have the reference, do not add it
            return
         }

         psiImportList.add(JavaPsiFacade.getElementFactory(project).createImportStatement(layoutInflaterPsiClass!!))
      }
   }

   /**
    * Adds a static import to the passed in psiFile.
    */
   fun addStaticImport(project: Project, psiFile: PsiFile, qualifiedName: String) {
      addStaticImport(project, psiFile, qualifiedName, "*")
   }

   /**
    * Adds a static import to the passed in psiFile.
    */
   fun addStaticImport(project: Project, psiFile: PsiFile, qualifiedName: String, referenceName: String) {
      val layoutInflaterPsiClass = JavaPsiFacade.getInstance(project).findClass(qualifiedName, GlobalSearchScope.allScope(project))
      val psiImportList = findElement(psiFile, PsiImportList::class.java)

      if (psiImportList != null) {
         if (psiImportList.children.any { i -> i is PsiImportStaticStatement
            && i.resolveTargetClass()!!.qualifiedName == qualifiedName
            && i.referenceName == if (referenceName == "*") null else referenceName }) {
            // we already have the reference, do not add it
            return
         }

         psiImportList.add(JavaPsiFacade.getElementFactory(project)
            .createImportStaticStatement(layoutInflaterPsiClass!!, referenceName))
      }
   }

   /**
    * Removes an import from the passed in psiFile.
    */
   fun removeImport(psiFile: PsiFile, qualifiedName: String) {
      val psiImportList = findElement(psiFile, PsiImportList::class.java)

      psiImportList
         ?.children
         ?.filter { qualifiedNamesEqual(it, qualifiedName) }
         ?.forEach { it.delete() }
   }

   /**
    * Removes all imports from the passed in psiFile that have paths
    * that start with the passed in qualifiedName.
    */
   fun removeImportStartsWith(psiFile: PsiFile, qualifiedName: String) {
      val psiImportList = findElement(psiFile, PsiImportList::class.java)

      psiImportList
         ?.children
         ?.filter { qualifiedNameStartsWith(it, qualifiedName) }
         ?.forEach { it.delete() }
   }

   /**
    * True if PsiElement is a PsiImportStatement or PsiImportStaticStatement and
    * the PsiElement's qualified name == the passed in qualifiedName.
    */
   fun qualifiedNamesEqual(psiElement: PsiElement,
                           qualifiedName: String) =
      (psiElement is PsiImportStatement && psiElement.qualifiedName == qualifiedName)
         || (psiElement is PsiImportStaticStatement && psiElement.importReference?.qualifiedName == qualifiedName)

   /**
    * True if PsiElement is a PsiImportStatement or PsiImportStaticStatement and
    * the PsiElement's qualified name starts with the passed in qualifiedName.
    */
   fun qualifiedNameStartsWith(psiElement: PsiElement,
                               qualifiedName: String) =
      (psiElement is PsiImportStatement && psiElement.qualifiedName.toString().startsWith(qualifiedName))
         || (psiElement is PsiImportStaticStatement && psiElement.importReference?.qualifiedName.toString().startsWith(qualifiedName))

   fun <T : PsiElement> findElement(psiElement: PsiElement, clazz: Class<T>): T? {
      val list = findElements(psiElement, clazz)
      return when {
         list.isNotEmpty() -> list[0]
         else -> null
      }
   }

   fun <T : PsiElement> findElements(psiElement: PsiElement, clazz: Class<T>): List<T> {
      val list = ArrayList<T>()
      psiElement.accept(object : PsiRecursiveElementVisitor() {
         override fun visitElement(element: PsiElement) {
            super.visitElement(element)
            if (clazz.isInstance(element)) {
               list.add(element as T)
            }
         }
      })

      return list
   }

   fun getMethodName(element: PsiElement): String? =
      if (element is PsiMethodCallExpression)
         MethodCallUtils.getMethodName(element)
      else
         null
}
