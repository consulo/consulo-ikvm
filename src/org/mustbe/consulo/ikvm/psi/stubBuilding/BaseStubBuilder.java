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
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 06.05.14
 */
public abstract class BaseStubBuilder<T extends PsiElement>
{
	protected final PsiElement myNavTarget;
	protected final List<String> myModifiers = new ArrayList<String>(2);
	protected final String myName;

	public BaseStubBuilder(PsiElement navTarget, String name)
	{
		myNavTarget = navTarget;
		myName = name;
	}

	public void addModifier(String mod)
	{
		myModifiers.add(mod);
	}

	@NotNull
	public static String normalize(String str)
	{
		if(str.indexOf('@') != -1)
		{
			return str.replace("@", "");
		}
		return str;
	}

	@NotNull
	public static String normalizeType(DotNetTypeRef type)
	{
		//TODO [VISTALL]
		if(type instanceof CSharpLambdaTypeRef)
		{
			return "Func";
		}
		else if(type instanceof CSharpPointerTypeRef)
		{
			return "Pointer";
		}
		else if(type == DotNetTypeRef.ERROR_TYPE)
		{
			return "error";
		}
		String qualifiedText = type.getQualifiedText();

		if(Comparing.equal(qualifiedText, "<error>"))
		{
			return "error";
		}
		qualifiedText = normalize(qualifiedText);

		return qualifiedText;
	}

	@NotNull
	public abstract T buildToPsi(@Nullable PsiElement parent);

	public abstract void buildToText(StringBuilder builder);
}
