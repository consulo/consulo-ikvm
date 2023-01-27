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

package consulo.ikvm.impl.psi;

import com.intellij.java.language.impl.psi.impl.file.PsiPackageImpl;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiElementFinder;
import com.intellij.java.language.psi.PsiJavaPackage;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetPsiSearcher;
import consulo.ikvm.impl.psi.stubBuilding.JavaClassStubBuilder;
import consulo.ikvm.impl.psi.stubBuilding.StubBuilder;
import consulo.java.language.module.extension.JavaModuleExtension;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiPackage;
import consulo.language.psi.PsiPackageManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 06.05.14
 */
@ExtensionImpl
public class IkvmPsiElementFinder extends PsiElementFinder
{
	private final Project myProject;
	private final PsiManager myPsiManager;
	private final PsiPackageManager myPsiPackageManager;

	@Inject
	public IkvmPsiElementFinder(Project project, PsiManager psiManager, PsiPackageManager psiPackageManager)
	{
		myProject = project;
		myPsiManager = psiManager;
		myPsiPackageManager = psiPackageManager;
	}

	@Nullable
	@Override
	@RequiredReadAction
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
	@RequiredReadAction
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
	@RequiredReadAction
	public PsiClass findClass(@NotNull String s, @NotNull GlobalSearchScope searchScope)
	{
		PsiClass[] aClass = findClasses(s, searchScope);
		return aClass.length == 0 ? null : aClass[0];
	}

	@NotNull
	@Override
	@RequiredReadAction
	public PsiClass[] findClasses(@NotNull String s, @NotNull GlobalSearchScope searchScope)
	{
		boolean cli = false;
		if(s.startsWith("cli."))
		{
			s = s.substring(4, s.length());
			cli = true;
		}
		DotNetTypeDeclaration[] types = DotNetPsiSearcher.getInstance(myProject).findTypes(s, searchScope);
		if(types.length == 0)
		{
			return PsiClass.EMPTY_ARRAY;
		}
		return toClasses(List.of(types), cli);
	}

	@NotNull
	@Override
	@RequiredReadAction
	public PsiClass[] getClasses(@NotNull PsiJavaPackage psiPackage, @NotNull GlobalSearchScope scope)
	{
		boolean cli = false;
		String qualifiedName = psiPackage.getQualifiedName();
		if(StringUtil.startsWith(qualifiedName, "cli."))
		{
			qualifiedName = qualifiedName.substring(4, qualifiedName.length());
			cli = true;
		}

		DotNetNamespaceAsElement namespace = DotNetPsiSearcher.getInstance(myProject).findNamespace(qualifiedName, scope);
		if(namespace == null)
		{
			return PsiClass.EMPTY_ARRAY;
		}

		Collection<PsiElement> children = namespace.getChildren(scope, DotNetNamespaceAsElement.ChildrenFilter.ONLY_ELEMENTS);

		if(children.isEmpty())
		{
			return PsiClass.EMPTY_ARRAY;
		}
		return toClasses(children, cli);
	}

	@RequiredReadAction
	private PsiClass[] toClasses(Collection<PsiElement> elements, boolean cli)
	{
		List<PsiClass> list = new ArrayList<>(elements.size());
		for(PsiElement dotNetNamedElement : elements)
		{
			if(dotNetNamedElement instanceof DotNetTypeDeclaration)
			{
				final DotNetTypeDeclaration type = (DotNetTypeDeclaration) dotNetNamedElement;
				if(!type.hasModifier(DotNetModifier.PUBLIC))
				{
					continue;
				}

				JavaClassStubBuilder build = StubBuilder.build(type);
				if(build == null)
				{
					continue;
				}

				PsiClass value = build.buildToPsi(null);
				list.add(value);
			}
		}
		return list.toArray(new PsiClass[list.size()]);
	}
}