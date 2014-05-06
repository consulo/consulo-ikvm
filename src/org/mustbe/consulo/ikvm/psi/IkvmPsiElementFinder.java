/*
 * Copyright 2013-2014 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mustbe.consulo.ikvm.psi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.MemberByNamespaceQNameIndex;
import org.mustbe.consulo.dotnet.psi.DotNetFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiFacade;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFinder;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaPackage;
import com.intellij.psi.impl.light.LightClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 06.05.14
 */
public class IkvmPsiElementFinder extends PsiElementFinder
{
	private final Project myProject;

	private Map<DotNetTypeDeclaration, PsiClass>  myCache = new HashMap<DotNetTypeDeclaration, PsiClass>();
	public IkvmPsiElementFinder(Project project)
	{
		myProject = project;
	}

	@Nullable
	@Override
	public PsiClass findClass(@NotNull String s, @NotNull GlobalSearchScope searchScope)
	{
		PsiClass[] aClass = findClasses(s, searchScope);
		return aClass.length == 0 ? null : aClass[0];
	}

	@NotNull
	@Override
	public PsiClass[] findClasses(@NotNull String s, @NotNull GlobalSearchScope searchScope)
	{
		DotNetTypeDeclaration[] types = DotNetPsiFacade.getInstance(myProject).findTypes(s, searchScope, -1);
		if(types.length == 0)
		{
			return PsiClass.EMPTY_ARRAY;
		}
		List<PsiClass> classes = new ArrayList<PsiClass>(types.length);
		for(int i = 0; i < types.length; i++)
		{
			final DotNetTypeDeclaration type = types[i];

			PsiClass psiClass = myCache.get(type);
			if(psiClass != null)
			{
				classes.add(psiClass);
			}
			else
			{
				if(type.getName().contains("$"))
				{
					continue;
				}
				StringBuilder builder = toJavaStub(type);
				PsiClass classFromText = createJavaFile(builder);

				LightClass lightClass = new LightClass(classFromText){
					@NotNull
					@Override
					public PsiElement getNavigationElement()
					{
						return type;
					}
				};
				lightClass.setNavigationElement(type);
				classes.add(lightClass);
				myCache.put(type, lightClass);
			}
		}
		return ContainerUtil.toArray(classes, PsiClass.ARRAY_FACTORY);
	}


	@NotNull
	@Override
	public PsiClass[] getClasses(
			@NotNull PsiJavaPackage psiPackage, @NotNull GlobalSearchScope scope)
	{


		Collection<DotNetNamedElement> dotNetNamedElements = MemberByNamespaceQNameIndex.getInstance().get(psiPackage.getQualifiedName(), myProject,
				scope);


		List<PsiClass> list = new ArrayList<PsiClass>();
		for(DotNetNamedElement dotNetNamedElement : dotNetNamedElements)
		{
			if(dotNetNamedElement instanceof DotNetTypeDeclaration)
			{
				final DotNetTypeDeclaration type = (DotNetTypeDeclaration) dotNetNamedElement;
				if(type.getName().contains("$"))
				{
					continue;
				}
				PsiClass psiClass = myCache.get(type);
				if(psiClass != null)
				{
					list.add(psiClass);
					continue;
				}
				StringBuilder builder = toJavaStub(type);
				PsiClass classFromText = createJavaFile(builder);

				LightClass lightClass = new LightClass(classFromText){
					@NotNull
					@Override
					public PsiElement getNavigationElement()
					{
						return type;
					}
				};
				//lightClass.setNavigationElement(type);

				myCache.put(type, lightClass);
				list.add(lightClass);
			}
		}
		return list.toArray(new PsiClass[list.size()]);
	}

	private StringBuilder toJavaStub(DotNetTypeDeclaration type)
	{
		StringBuilder builder = new StringBuilder();

		String presentableParentQName = type.getPresentableParentQName();
		if(!StringUtil.isEmpty(presentableParentQName))
		{
			builder.append("package ").append(presentableParentQName).append(";");
		}

		builder.append("public class ").append(type.getName()).append(" {\n");

		for(DotNetNamedElement dotNetNamedElement : type.getMembers())
		{
			if(dotNetNamedElement instanceof DotNetFieldDeclaration)
			{
				builder.append("public ").append(((DotNetFieldDeclaration) dotNetNamedElement).getType().toTypeRef().getQualifiedText()).append(" " +
						"").append(dotNetNamedElement.getName()).append(";\n");
			}
		}
		builder.append("}");
		return builder;
	}

	private PsiClass createJavaFile(StringBuilder text)
	{
		PsiFile fileFromText = PsiFileFactory.getInstance(myProject).createFileFromText("dummy.java", JavaFileType.INSTANCE, text);
		return PsiTreeUtil.findChildOfType(fileFromText, PsiClass.class, false);
	}
}
