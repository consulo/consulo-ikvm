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

package consulo.ikvm.impl.psi;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.search.PsiShortNameProvider;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.function.CommonProcessors;
import consulo.application.util.function.Processor;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetShortNameSearcher;
import consulo.ikvm.impl.psi.stubBuilding.JavaClassStubBuilder;
import consulo.ikvm.impl.psi.stubBuilding.StubBuilder;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.IdFilter;
import consulo.project.Project;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.ContainerUtil;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * @author VISTALL
 * @since 13.05.2015
 */
@ExtensionImpl
public class IkvmPsiShortNamesCache implements PsiShortNameProvider
{
	private final Project myProject;

	@Inject
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
			JavaClassStubBuilder javaClassStubBuilder = StubBuilder.build(dotNetTypeDeclaration);
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
