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

package org.mustbe.consulo.ikvm.psi.stubBuilding;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 09.05.14
 */
public class JavaMethodStubBuilder extends BaseStubBuilder<PsiMethod>
{
	private DotNetTypeRef myReturnType;
	private List<JavaParameterStubBuilder> myParameters = new ArrayList<JavaParameterStubBuilder>(5);

	public JavaMethodStubBuilder(PsiElement navTarget, String name)
	{
		super(navTarget, name);
	}

	@NotNull
	@Override
	public PsiMethod buildToPsi(@Nullable PsiElement parent)
	{
		PsiManager psiManager = PsiManager.getInstance(myNavTarget.getProject());
		LightMethodBuilder builder = new LightMethodBuilder(psiManager, myName);
		builder.setModifiers(ArrayUtil.toStringArray(myModifiers));
		builder.setContainingClass((PsiClass) parent);
		builder.setMethodReturnType(normalizeType(myReturnType));
		for(JavaParameterStubBuilder parameter : myParameters)
		{
			builder.addParameter(parameter.buildToPsi(builder));
		}

		return builder;
	}

	@Override
	public void buildToText(StringBuilder builder)
	{
		for(String modifier : myModifiers)
		{
			builder.append(modifier).append(" ");
		}
		builder.append(normalizeType(myReturnType)).append(" ").append(normalize(myName)).append("(");
		for(int i = 0; i < myParameters.size(); i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}
			JavaParameterStubBuilder javaParameterStubBuilder = myParameters.get(i);
			javaParameterStubBuilder.buildToText(builder);
		}
		builder.append(") {}");
	}

	@NotNull
	public JavaMethodStubBuilder withParameter(@NotNull DotNetTypeRef typeRef, @NotNull String name, @NotNull PsiElement navTarget)
	{
		JavaParameterStubBuilder p = new JavaParameterStubBuilder(navTarget, name);
		p.withType(typeRef);
		myParameters.add(p);
		return this;
	}

	public void withReturnType(DotNetTypeRef returnTypeRef)
	{
		myReturnType = returnTypeRef;
	}
}
