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
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNativeTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.java.util.JavaClassNames;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;

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
	public PsiType normalizeType(DotNetTypeRef type)
	{
		if(type == CSharpNativeTypeRef.VOID)
		{
			return PsiType.VOID;
		}
		else if(type == CSharpNativeTypeRef.SBYTE)
		{
			return PsiType.BYTE;
		}
		else if(type == CSharpNativeTypeRef.INT)
		{
			return PsiType.INT;
		}
		else if(type == CSharpNativeTypeRef.SHORT)
		{
			return PsiType.SHORT;
		}
		else if(type == CSharpNativeTypeRef.BOOL)
		{
			return PsiType.BOOLEAN;
		}
		else if(type == CSharpNativeTypeRef.LONG)
		{
			return PsiType.LONG;
		}
		else if(type == CSharpNativeTypeRef.FLOAT)
		{
			return PsiType.FLOAT;
		}
		else if(type == CSharpNativeTypeRef.DOUBLE)
		{
			return PsiType.DOUBLE;
		}
		else if(type == CSharpNativeTypeRef.CHAR)
		{
			return PsiType.CHAR;
		}
		else if(type == CSharpNativeTypeRef.STRING)
		{
			return fromText(JavaClassNames.JAVA_LANG_STRING);
		}
		else if(type == CSharpNativeTypeRef.OBJECT)
		{
			return fromText(JavaClassNames.JAVA_LANG_OBJECT);
		}
		else if(type instanceof CSharpArrayTypeRef)
		{
			return new PsiArrayType(normalizeType(((CSharpArrayTypeRef) type).getInnerType()));
		}

		//TODO [VISTALL]
		if(type instanceof CSharpLambdaTypeRef)
		{
			return fromText("Func");
		}
		else if(type instanceof CSharpPointerTypeRef)
		{
			return fromText("Pointer");
		}
		else if(type == DotNetTypeRef.ERROR_TYPE)
		{
			return fromText("error");
		}
		String qualifiedText = type.getQualifiedText();

		if(Comparing.equal(qualifiedText, "<error>"))
		{
			return fromText("error");
		}
		qualifiedText = normalize(qualifiedText);

		return fromText(qualifiedText);
	}

	private PsiType fromText(String text)
	{
		try
		{
			return JavaPsiFacade.getElementFactory(myNavTarget.getProject()).createTypeFromText(text, null);
		}
		catch(Exception e)
		{
			return PsiType.VOID;
		}
	}

	@NotNull
	public String normalizeTypeText(DotNetTypeRef type)
	{
		PsiType psiType = normalizeType(type);
		return psiType.getCanonicalText();
	}

	@NotNull
	public abstract T buildToPsi(@Nullable PsiElement parent);

	public abstract void buildToText(StringBuilder builder);
}
