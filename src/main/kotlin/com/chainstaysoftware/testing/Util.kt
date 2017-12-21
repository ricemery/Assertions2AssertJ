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
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.siyeh.ig.psiutils.MethodCallUtils
import java.util.ArrayList


object Util {
   fun traverseTestFiles(project: Project,
                         iterator: (PsiFile) -> Unit) {
      val projectFileIndex = ProjectFileIndex.SERVICE.getInstance(project)

      val files = ApplicationManager.getApplication().runReadAction(Computable {
         FileBasedIndex.getInstance().getContainingFiles(
            FileTypeIndex.NAME,
            JavaFileType.INSTANCE,
            GlobalSearchScope.projectScope(project))
            .filter { virtualFile -> projectFileIndex.isInTestSourceContent(virtualFile) }
            .map { virtualFile -> PsiManager.getInstance(project).findFile(virtualFile) }
      })

      files.forEach{ psiFile -> if (psiFile != null) iterator(psiFile) }
   }

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

   fun removeImport(psiFile: PsiFile, qualifiedName: String) {
      val psiImportList = findElement(psiFile, PsiImportList::class.java)

      psiImportList
         ?.children
         ?.filter { it is PsiImportStatement && it.qualifiedName == qualifiedName }
         ?.forEach { it.delete() }
   }

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
