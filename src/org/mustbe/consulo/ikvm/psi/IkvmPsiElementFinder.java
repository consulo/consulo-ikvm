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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.consulo.java.module.extension.JavaModuleExtension;
import org.consulo.psi.PsiPackage;
import org.consulo.psi.PsiPackageManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpNamespaceHelper;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.MemberByNamespaceQNameIndex;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiFacade;
import org.mustbe.consulo.ikvm.psi.stubBuilding.JavaClassStubBuilder;
import org.mustbe.consulo.ikvm.psi.stubBuilding.StubBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFinder;
import com.intellij.psi.PsiJavaPackage;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiPackageImpl;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * @author VISTALL
 * @since 06.05.14
 */
public class IkvmPsiElementFinder extends PsiElementFinder
{
	private final Project myProject;
	private final PsiManager myPsiManager;
	private final PsiPackageManager myPsiPackageManager;

	private Map<DotNetTypeDeclaration, PsiClass> myCache = new HashMap<DotNetTypeDeclaration, PsiClass>();

	public IkvmPsiElementFinder(Project project, PsiManager psiManager, PsiPackageManager psiPackageManager)
	{
		myProject = project;
		myPsiManager = psiManager;
		myPsiPackageManager = psiPackageManager;
	}

	@Nullable
	@Override
	public PsiJavaPackage findPackage(@NotNull String qualifiedName)
	{
		if(qualifiedName.equals("cli"))
		{
			return new PsiPackageImpl(myPsiManager, myPsiPackageManager, JavaModuleExtension.class, qualifiedName);
		}
		if(qualifiedName.startsWith("cli."))
		{
			PsiPackage aPackage = PsiPackageManager.getInstance(myProject).findPackage(qualifiedName.substring(4, qualifiedName.length()),
					JavaModuleExtension.class);
			if(aPackage != null)
			{
				return new PsiPackageImpl(myPsiManager, myPsiPackageManager, JavaModuleExtension.class, qualifiedName);
			}
		}

		return super.findPackage(qualifiedName);
	}

	@NotNull
	@Override
	public PsiJavaPackage[] getSubPackages(@NotNull PsiJavaPackage psiPackage, @NotNull GlobalSearchScope scope)
	{
		String qualifiedName = psiPackage.getQualifiedName();
		if(qualifiedName.startsWith("cli"))
		{
			String substring = qualifiedName.substring(3, qualifiedName.length());
			PsiPackage aPackage = PsiPackageManager.getInstance(myProject).findPackage(substring, DotNetModuleExtension.class);
			if(aPackage == null)
			{
				aPackage = PsiPackageManager.getInstance(myProject).findPackage(substring, JavaModuleExtension.class);
			}
			if(aPackage != null)
			{
				PsiPackage[] subPackages = aPackage.getSubPackages(scope);
				if(subPackages.length == 0)
				{
					return PsiJavaPackage.EMPTY_ARRAY;
				}
				PsiJavaPackage[] packages = new PsiJavaPackage[subPackages.length];
				for(int i = 0; i < subPackages.length; i++)
				{
					PsiPackage subPackage = subPackages[i];
					packages[i] = new PsiPackageImpl(myPsiManager, myPsiPackageManager, JavaModuleExtension.class, subPackage.getQualifiedName());
				}
				return packages;
			}
		}
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
		boolean cli = false;
		if(s.startsWith("cli."))
		{
			s = s.substring(4, s.length());
			cli = true;
		}
		DotNetTypeDeclaration[] types = DotNetPsiFacade.getInstance(myProject).findTypes(s, searchScope, -1);
		if(types.length == 0)
		{
			return PsiClass.EMPTY_ARRAY;
		}
		return toClasses(Arrays.asList(types), cli);
	}

	@NotNull
	@Override
	public PsiClass[] getClasses(@NotNull PsiJavaPackage psiPackage, @NotNull GlobalSearchScope scope)
	{
		boolean cli = false;
		String qualifiedName = psiPackage.getQualifiedName();
		if(StringUtil.startsWith(qualifiedName, "cli."))
		{
			qualifiedName = CSharpNamespaceHelper.getNamespaceForIndexing(qualifiedName.substring(4, qualifiedName.length()));
			cli = true;
		}

		Collection<DotNetNamedElement> dotNetNamedElements = MemberByNamespaceQNameIndex.getInstance().get(qualifiedName, myProject,
				scope);

		if(dotNetNamedElements.isEmpty())
		{
			return PsiClass.EMPTY_ARRAY;
		}
		return toClasses(dotNetNamedElements, cli);
	}

	private PsiClass[] toClasses(Collection<? extends DotNetNamedElement> elements, boolean cli)
	{
		List<PsiClass> list = new ArrayList<PsiClass>(elements.size());
		for(DotNetNamedElement dotNetNamedElement : elements)
		{
			if(dotNetNamedElement instanceof DotNetTypeDeclaration)
			{
				final DotNetTypeDeclaration type = (DotNetTypeDeclaration) dotNetNamedElement;
				if(!type.hasModifier(CSharpModifier.PUBLIC))
				{
					continue;
				}
				PsiClass psiClass = myCache.get(type);
				if(psiClass != null)
				{
					list.add(psiClass);
					continue;
				}

				JavaClassStubBuilder build = StubBuilder.build(type, cli);
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