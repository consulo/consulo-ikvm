/*
 * Copyright 2013-2015 must-be.org
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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetShortNameSearcher;
import org.mustbe.consulo.ikvm.psi.stubBuilding.JavaClassStubBuilder;
import org.mustbe.consulo.ikvm.psi.stubBuilding.StubBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashSet;
import com.intellij.util.indexing.IdFilter;

/**
 * @author VISTALL
 * @since 13.05.2015
 */
public class IkvmPsiShortNamesCache extends PsiShortNamesCache
{
	private final Project myProject;

	public IkvmPsiShortNamesCache(Project project)
	{
		myProject = project;
	}

	@NotNull
	@Override
	@RequiredReadAction
	public PsiClass[] getClassesByName(@NotNull @NonNls String s, @NotNull GlobalSearchScope globalSearchScope)
	{
		CommonProcessors.CollectProcessor<DotNetTypeDeclaration> collectProcessor = new CommonProcessors.CollectProcessor<DotNetTypeDeclaration>();
		DotNetShortNameSearcher.getInstance(myProject).collectTypes(s, globalSearchScope, IdFilter.getProjectIdFilter(myProject, false),
				collectProcessor);
		Collection<DotNetTypeDeclaration> results = collectProcessor.getResults();
		List<PsiClass> classes = new ArrayList<PsiClass>(results.size());
		for(DotNetTypeDeclaration dotNetTypeDeclaration : results)
		{
			JavaClassStubBuilder javaClassStubBuilder = StubBuilder.build(dotNetTypeDeclaration, false);
			if(javaClassStubBuilder == null)
			{
				continue;
			}
			classes.add(javaClassStubBuilder.buildToPsi(null));
		}
		return ContainerUtil.toArray(classes, PsiClass.ARRAY_FACTORY);
	}

	@NotNull
	@Override
	public String[] getAllClassNames()
	{
		HashSet<String> set = new HashSet<String>();
		getAllClassNames(set);
		return ArrayUtil.toStringArray(set);
	}

	@Override
	public void getAllClassNames(@NotNull HashSet<String> strings)
	{
		DotNetShortNameSearcher.getInstance(myProject).collectTypeNames(new CommonProcessors.CollectProcessor<String>(strings),
				GlobalSearchScope.allScope(myProject), IdFilter.getProjectIdFilter(myProject, false));
	}

	@NotNull
	@Override
	public PsiMethod[] getMethodsByName(@NonNls @NotNull String s, @NotNull GlobalSearchScope globalSearchScope)
	{
		return new PsiMethod[0];
	}

	@NotNull
	@Override
	public PsiMethod[] getMethodsByNameIfNotMoreThan(@NonNls @NotNull String s, @NotNull GlobalSearchScope globalSearchScope, int i)
	{
		return new PsiMethod[0];
	}

	@NotNull
	@Override
	public PsiField[] getFieldsByNameIfNotMoreThan(@NonNls @NotNull String s, @NotNull GlobalSearchScope globalSearchScope, int i)
	{
		return new PsiField[0];
	}

	@Override
	public boolean processMethodsWithName(@NonNls @NotNull String s,
			@NotNull GlobalSearchScope globalSearchScope,
			@NotNull Processor<PsiMethod> psiMethodProcessor)
	{
		return false;
	}

	@Override
	public boolean processMethodsWithName(@NonNls @NotNull String s,
			@NotNull Processor<? super PsiMethod> processor,
			@NotNull GlobalSearchScope globalSearchScope,
			@Nullable IdFilter idFilter)
	{
		return false;
	}

	@NotNull
	@Override
	public String[] getAllMethodNames()
	{
		return new String[0];
	}

	@Override
	public void getAllMethodNames(@NotNull HashSet<String> strings)
	{

	}

	@NotNull
	@Override
	public PsiField[] getFieldsByName(@NotNull @NonNls String s, @NotNull GlobalSearchScope globalSearchScope)
	{
		return new PsiField[0];
	}

	@NotNull
	@Override
	public String[] getAllFieldNames()
	{
		return new String[0];
	}

	@Override
	public void getAllFieldNames(@NotNull HashSet<String> strings)
	{

	}

	@Override
	public boolean processFieldsWithName(@NotNull String s,
			@NotNull Processor<? super PsiField> processor,
			@NotNull GlobalSearchScope globalSearchScope,
			@Nullable IdFilter idFilter)
	{
		return false;
	}

	@Override
	public boolean processClassesWithName(@NotNull String s,
			@NotNull Processor<? super PsiClass> processor,
			@NotNull GlobalSearchScope globalSearchScope,
			@Nullable IdFilter idFilter)
	{
		return false;
	}
}
