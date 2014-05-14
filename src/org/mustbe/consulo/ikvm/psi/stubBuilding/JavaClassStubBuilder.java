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
import org.mustbe.consulo.ikvm.psi.stubBuilding.psi.LightJavaClassBuilder;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 06.05.14
 */
public class JavaClassStubBuilder extends BaseStubBuilder<PsiClass>
{
	private final List<BaseStubBuilder> myMembers = new ArrayList<BaseStubBuilder>();
	private final String myPackage;

	public JavaClassStubBuilder(@Nullable String packageName, @NotNull String name, @NotNull PsiElement navTarget)
	{
		super(navTarget, name);
		myPackage = packageName;
	}

	@NotNull
	public JavaFieldStubBuilder field(@NotNull String name, PsiElement navTarget)
	{
		JavaFieldStubBuilder fieldStubBuilder = new JavaFieldStubBuilder(navTarget, name);
		myMembers.add(fieldStubBuilder);
		return fieldStubBuilder;
	}

	public JavaMethodStubBuilder method(@NotNull String name, PsiElement navTarget)
	{
		JavaMethodStubBuilder methodStubBuilder = new JavaMethodStubBuilder(navTarget, name);
		myMembers.add(methodStubBuilder);
		return methodStubBuilder;
	}

	@NotNull
	@Override
	public PsiClass buildToPsi(@Nullable PsiElement parent)
	{
		LightJavaClassBuilder builder = new LightJavaClassBuilder(myNavTarget.getProject());
		builder.withPackage(normalize(myPackage));
		builder.withName(normalize(myName));
		builder.withModifiers(ArrayUtil.toStringArray(myModifiers));
		builder.setNavigationElement(myNavTarget);

		List<PsiField> fields = new ArrayList<PsiField>(5);
		List<PsiMethod> methods = new ArrayList<PsiMethod>(5);
		for(BaseStubBuilder member : myMembers)
		{
			if(member instanceof JavaFieldStubBuilder)
			{
				fields.add((PsiField) member.buildToPsi(builder));
			}
			else if(member instanceof JavaMethodStubBuilder)
			{
				methods.add((PsiMethod) member.buildToPsi(builder));
			}
		}
		builder.withFields(fields);
		builder.withMethods(methods);

		return builder;
	}

	@Override
	public void buildToText(StringBuilder builder)
	{
		if(!StringUtil.isEmpty(myPackage))
		{
			builder.append("package ").append(normalize(myPackage)).append(";\n");
		}

		for(String modifier : myModifiers)
		{
			builder.append(modifier).append(" ");
		}

		builder.append("class ").append(normalize(myName)).append(" ");

		builder.append("{\n");
		for(BaseStubBuilder member : myMembers)
		{
			builder.append("\t");
			member.buildToText(builder);
			builder.append("\n");
		}
		builder.append("}");
	}

	public byte[] buildToBytecode()
	{
		ClassWriter classWriter = new ClassWriter(Opcodes.V1_6);
		buildToBytecode(classWriter);
		classWriter.visitEnd();
		return classWriter.toByteArray();
	}

	@Override
	public void buildToBytecode(ClassWriter parent)
	{
		String name = null;
		if(StringUtil.isEmpty(myPackage))
		{
			name = myName;
		}
		else
		{
			name = myPackage.replace(".", "/") + "/" + myName;
		}
		parent.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, name, null, null, null);

		for(BaseStubBuilder member : myMembers)
		{
			member.buildToBytecode(parent);
		}
	}

	public String getQualifiedName()
	{
		String name = null;
		if(StringUtil.isEmpty(myPackage))
		{
			name = myName;
		}
		else
		{
			name = myPackage + "." + myName;
		}
		return name;
	}
}
