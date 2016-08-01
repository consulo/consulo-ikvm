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

package consulo.ikvm.psi.stubBuilding;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.resolve.DotNetArrayTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.java.util.JavaClassNames;
import org.objectweb.asm.ClassWriter;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiImmediateClassType;

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

	public String getName()
	{
		return myName;
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
		String qName = type.getQualifiedText();
		if(Comparing.equal(qName, "System.Void"))
		{
			return PsiType.VOID;
		}
		else if(Comparing.equal(qName, "System.Byte"))
		{
			return PsiType.BYTE;
		}
		else if(Comparing.equal(qName, "System.Int32"))
		{
			return PsiType.INT;
		}
		else if(Comparing.equal(qName, "System.Int16"))
		{
			return PsiType.SHORT;
		}
		else if(Comparing.equal(qName, "System.Boolean"))
		{
			return PsiType.BOOLEAN;
		}
		else if(Comparing.equal(qName, "System.Int64"))
		{
			return PsiType.LONG;
		}
		else if(Comparing.equal(qName, "System.Single"))
		{
			return PsiType.FLOAT;
		}
		else if(Comparing.equal(qName, "System.Double"))
		{
			return PsiType.DOUBLE;
		}
		else if(Comparing.equal(qName, "System.Char"))
		{
			return PsiType.CHAR;
		}
		else if(Comparing.equal(qName, "System.String"))
		{
			return fromQName(JavaClassNames.JAVA_LANG_STRING);
		}
		else if(Comparing.equal(qName, "System.Object"))
		{
			return fromQName(JavaClassNames.JAVA_LANG_OBJECT);
		}
		else if(type instanceof DotNetArrayTypeRef)
		{
			return new PsiArrayType(normalizeType(((DotNetArrayTypeRef) type).getInnerTypeRef()));
		}

		//TODO [VISTALL]
		if(type instanceof DotNetPointerTypeRef)
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

	@NotNull
	private PsiType fromQName(String qName)
	{
		PsiClass aClass = JavaPsiFacade.getInstance(myNavTarget.getProject()).findClass(qName, myNavTarget.getResolveScope());
		if(aClass == null)
		{
			return fromText(qName);
		}
		return new PsiImmediateClassType(aClass, PsiSubstitutor.EMPTY);
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

	@RequiredReadAction
	public abstract void buildToText(@NotNull StringBuilder builder, @Nullable BaseStubBuilder<?> parent);

	public abstract void buildToBytecode(ClassWriter parent);
}
