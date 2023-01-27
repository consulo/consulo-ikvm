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

package consulo.ikvm.impl.psi.stubBuilding;

import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.ikvm.impl.psi.stubBuilding.psi.DotNetTypeToJavaClass;
import consulo.internal.org.objectweb.asm.ClassWriter;
import consulo.internal.org.objectweb.asm.Opcodes;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 06.05.14
 */
public class JavaClassStubBuilder extends BaseStubBuilder<PsiClass>
{
	private final String myPackage;

	public JavaClassStubBuilder(@Nullable String packageName, @NotNull String name, @NotNull DotNetTypeDeclaration navTarget)
	{
		super(navTarget, name);
		myPackage = packageName;
	}

	@NotNull
	@Override
	public PsiClass buildToPsi(@Nullable PsiElement parent)
	{
		final DotNetTypeToJavaClass builder = new DotNetTypeToJavaClass((DotNetTypeDeclaration) myNavTarget);
		builder.withPackage(normalize(myPackage));
		builder.withName(normalize(myName));
		builder.withModifiers(ArrayUtil.toStringArray(myModifiers));
		builder.setNavigationElement(myNavTarget);
		return builder;
	}

	@RequiredReadAction
	@Override
	public void buildToText(@NotNull final StringBuilder builder, BaseStubBuilder<?> parent)
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
		StubBuilder.processMembers((DotNetTypeDeclaration) myNavTarget, new Consumer<BaseStubBuilder<?>>()
		{
			@Override
			@RequiredReadAction
			public void accept(BaseStubBuilder<?> member)
			{
				builder.append("\t");
				member.buildToText(builder, JavaClassStubBuilder.this);
				builder.append("\n");
			}
		});
		builder.append("}");
	}

	@RequiredReadAction
	public byte[] buildToBytecode()
	{
		ClassWriter classWriter = new ClassWriter(Opcodes.V1_6);
		buildToBytecode(classWriter);
		classWriter.visitEnd();
		return classWriter.toByteArray();
	}

	@Override
	@RequiredReadAction
	public void buildToBytecode(final ClassWriter parent)
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

		StubBuilder.processMembers((DotNetTypeDeclaration) myNavTarget, new Consumer<BaseStubBuilder<?>>()
		{
			@Override
			public void accept(BaseStubBuilder<?> baseStubBuilder)
			{
				baseStubBuilder.buildToBytecode(parent);
			}
		});
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
