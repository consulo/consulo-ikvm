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
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightMethodDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightTypeDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNativeTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiFacade;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFinder;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.impl.java.stubs.index.JavaShortClassNameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiMethodUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 08.05.14
 */
public class IkvmDotNetPsiFacade extends DotNetPsiFacade.Adapter
{
	private final Project myProject;

	public IkvmDotNetPsiFacade(Project project)
	{
		myProject = project;
	}

	@NotNull
	@Override
	public DotNetTypeDeclaration[] findTypes(@NotNull String qName, @NotNull GlobalSearchScope searchScope, int genericCount)
	{
		List<DotNetTypeDeclaration> list = new ArrayList<DotNetTypeDeclaration>(2);
		for(PsiElementFinder psiElementFinder : PsiElementFinder.EP_NAME.getExtensions(myProject))
		{
			if(psiElementFinder instanceof IkvmPsiElementFinder)
			{
				continue;
			}
			PsiClass[] classes1 = psiElementFinder.findClasses(qName, searchScope);
			for(PsiClass psiClass : classes1)
			{
				if(genericCount != -1 && psiClass.getTypeParameters().length != genericCount)
				{
					continue;
				}
				list.add(convert(psiClass));
			}
		}
		return ContainerUtil.toArray(list, DotNetTypeDeclaration.ARRAY_FACTORY);
	}

	@NotNull
	@Override
	public String[] getAllTypeNames()
	{
		Collection<String> allKeys = JavaShortClassNameIndex.getInstance().getAllKeys(myProject);
		return ArrayUtil.toStringArray(allKeys);
	}

	@NotNull
	@Override
	public DotNetTypeDeclaration[] getTypesByName(@NotNull String name, @NotNull GlobalSearchScope searchScope)
	{
		Collection<PsiClass> psiClasses = JavaShortClassNameIndex.getInstance().get(name, myProject, searchScope);

		if(psiClasses.isEmpty())
		{
			return DotNetTypeDeclaration.EMPTY_ARRAY;
		}
		PsiClass[] classes = ContainerUtil.toArray(psiClasses, PsiClass.ARRAY_FACTORY);
		DotNetTypeDeclaration[] types = new DotNetTypeDeclaration[psiClasses.size()];
		for(int i = 0; i < types.length; i++)
		{
			types[i] = convert(classes[i]);
		}
		return types;
	}

	@NotNull
	private DotNetTypeDeclaration convert(PsiClass aClass)
	{
		CSharpLightTypeDeclarationBuilder typeDeclarationBuilder = new CSharpLightTypeDeclarationBuilder(myProject);
		typeDeclarationBuilder.withName(aClass.getName());
		if(aClass.isEnum())
		{
			typeDeclarationBuilder.withType(CSharpLightTypeDeclarationBuilder.Type.ENUM);
		}
		else if(aClass.isInterface())
		{
			typeDeclarationBuilder.withType(CSharpLightTypeDeclarationBuilder.Type.INTERFACE);
		}

		if(aClass.hasModifierProperty(PsiModifier.PUBLIC))
		{
			typeDeclarationBuilder.addModifier(CSharpModifier.PUBLIC);
		}

		for(PsiMethod psiMethod : aClass.getMethods())
		{
			if(PsiMethodUtil.isMainMethod(psiMethod))
			{
				CSharpLightMethodDeclarationBuilder methodDeclarationBuilder = new CSharpLightMethodDeclarationBuilder(myProject);
				methodDeclarationBuilder.withName("Main");
				methodDeclarationBuilder.withReturnType(CSharpNativeTypeRef.VOID);
				methodDeclarationBuilder.addModifier(CSharpModifier.STATIC);
				methodDeclarationBuilder.addModifier(CSharpModifier.PUBLIC);

				/*CSharpLightParameterBuilder parameterBuilder = new CSharpLightParameterBuilder(myProject);
				parameterBuilder.withName("p");
				parameterBuilder.withTypeRef(new CSharpArrayTypeRef(new CSharpTypeDefTypeRef("System.String", 0), 0));
				methodDeclarationBuilder.addParameter(parameterBuilder);  */

				typeDeclarationBuilder.addMember(methodDeclarationBuilder);
			}
		}
		return typeDeclarationBuilder;
	}
}
