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
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.resolve.DotNetArrayTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.ikvm.psi.stubBuilding.psi.IkvmTypeRef;
import org.mustbe.consulo.java.util.JavaClassNames;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.util.ArrayUtil;
import com.intellij.util.BitUtil;

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
		builder.setNavigationElement(myNavTarget);
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
		builder.append(normalizeTypeText(myReturnType)).append(" ").append(normalize(myName)).append("(");
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

	@Override
	public void buildToBytecode(ClassWriter parent)
	{
		int access = 0;
		access = BitUtil.set(access, Opcodes.ACC_STATIC, myModifiers.contains(PsiModifier.STATIC));
		access = BitUtil.set(access, Opcodes.ACC_PUBLIC, myModifiers.contains(PsiModifier.PUBLIC));

		StringBuilder descBuilder = new StringBuilder();
		descBuilder.append("(");
		for(JavaParameterStubBuilder parameter : myParameters)
		{
			appendType(parameter.getType(), descBuilder);
		}
		descBuilder.append(")");
		appendType(myReturnType, descBuilder);

		try
		{
			parent.visitMethod(access, myName, descBuilder.toString(), null, null).visitEnd();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void appendType(DotNetTypeRef typeRef, StringBuilder builder)
	{
		String qualifiedText = typeRef.getQualifiedText();
		if("System.Void".equals(qualifiedText))
		{
			builder.append("V");
		}
		else if("System.Int32".equals(qualifiedText))
		{
			builder.append("I");
		}
		else if("System.String".equals(qualifiedText))
		{
			appendType(new IkvmTypeRef(JavaClassNames.JAVA_LANG_STRING), builder);
		}
		else if(typeRef instanceof DotNetGenericWrapperTypeRef)
		{
			appendType(((DotNetGenericWrapperTypeRef) typeRef).getInnerTypeRef(), builder);
		}
		else if(typeRef instanceof DotNetArrayTypeRef)
		{
			builder.append("[");
			appendType(((DotNetArrayTypeRef) typeRef).getInnerTypeRef(), builder);
		}
		else
		{
			if(qualifiedText.contains("<"))
			{
				//TODO
				appendType(new IkvmTypeRef(JavaClassNames.JAVA_LANG_OBJECT), builder);
			}
			else
			{
				if(qualifiedText.equals(DotNetTypes.System.Object))
				{
					appendType(new IkvmTypeRef(JavaClassNames.JAVA_LANG_OBJECT), builder);
					return;
				}
				if(!qualifiedText.startsWith("java"))
				{
					qualifiedText = "cli." + qualifiedText;
				}
				builder.append("L").append(qualifiedText.replace(".", "/")).append(";");
			}
		}
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
