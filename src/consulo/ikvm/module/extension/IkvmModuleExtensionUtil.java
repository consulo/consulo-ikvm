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

package consulo.ikvm.module.extension;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.psi.util.PsiMethodUtil;
import com.intellij.util.Query;
import com.intellij.util.containers.ContainerUtil;
import consulo.ikvm.psi.IkvmJavaClassAsDotNetTypeElement;

/**
 * @author VISTALL
 * @since 25.09.14
 */
public class IkvmModuleExtensionUtil
{
	@NotNull
	public static PsiElement[] buildEntryPoints(@NotNull Module module)
	{
		Query<PsiClass> search = AllClassesSearch.search(GlobalSearchScope.moduleScope(module), module.getProject());

		final List<PsiElement> list = new ArrayList<PsiElement>();
		search.forEach(psiClass -> {
			if(PsiMethodUtil.hasMainMethod(psiClass))
			{
				list.add(new IkvmJavaClassAsDotNetTypeElement(psiClass));
			}
			return true;
		});
		return ContainerUtil.toArray(list, PsiElement.ARRAY_FACTORY);
	}
}
