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

import org.consulo.java.module.extension.JavaModuleExtension;
import org.consulo.psi.PsiPackage;
import org.consulo.psi.PsiPackageManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.MemberByNamespaceQNameIndex;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiFacade;
import org.mustbe.consulo.ikvm.psi.stubBuilding.JavaClassStubBuilder;
import org.mustbe.consulo.ikvm.psi.stubBuilding.StubBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFinder;
import com.intellij.psi.PsiJavaPackage;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiPackageImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 06.05.14
 */
public class IkvmPsiElementFinder extends PsiElementFinder
{
	private final Project myProject;

	private Map<DotNetTypeDeclaration, PsiClass> myCache = new HashMap<DotNetTypeDeclaration, PsiClass>();

	public IkvmPsiElementFinder(Project project)
	{
		myProject = project;
	}

	@Nullable
	@Override
	public PsiJavaPackage findPackage(@NotNull String qualifiedName)
	{
		if(qualifiedName.equals("cli"))
		{
			return new PsiPackageImpl(PsiManager.getInstance(myProject), PsiPackageManager.getInstance(myProject), JavaModuleExtension.class, qualifiedName);
		}
		return super.findPackage(qualifiedName);
	}

	@NotNull
	@Override
	public PsiPackage[] getSubPackages(
			@NotNull PsiJavaPackage psiPackage, @NotNull GlobalSearchScope scope)
	{
		System.out.println(psiPackage.getQualifiedName());
		return super.getSubPackages(psiPackage, scope);
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
				JavaClassStubBuilder build = StubBuilder.build(type);
				if(build == null)
				{
					continue;
				}
				PsiClass value = build.buildToPsi(null);
				classes.add(value);
				myCache.put(type, value);
			}
		}
		return ContainerUtil.toArray(classes, PsiClass.ARRAY_FACTORY);
	}


	@NotNull
	@Override
	public PsiClass[] getClasses(@NotNull PsiJavaPackage psiPackage, @NotNull GlobalSearchScope scope)
	{
		Collection<DotNetNamedElement> dotNetNamedElements = MemberByNamespaceQNameIndex.getInstance().get(psiPackage.getQualifiedName(), myProject,
				scope);

		List<PsiClass> list = new ArrayList<PsiClass>();
		for(DotNetNamedElement dotNetNamedElement : dotNetNamedElements)
		{
			if(dotNetNamedElement instanceof DotNetTypeDeclaration)
			{
				final DotNetTypeDeclaration type = (DotNetTypeDeclaration) dotNetNamedElement;
				PsiClass psiClass = myCache.get(type);
				if(psiClass != null)
				{
					list.add(psiClass);
					continue;
				}

				JavaClassStubBuilder build = StubBuilder.build(type);
				if(build == null)
				{
					continue;
				}

				PsiClass value = build.buildToPsi(null);
				myCache.put(type, value);
				list.add(value);
			}
		}
		return list.toArray(new PsiClass[list.size()]);
	}
}