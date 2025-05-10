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

import com.intellij.java.language.impl.psi.impl.light.LightMethodBuilder;
import com.intellij.java.language.psi.CommonClassNames;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifier;
import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetArrayTypeRef;
import consulo.dotnet.psi.resolve.DotNetGenericWrapperTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.ikvm.impl.psi.stubBuilding.psi.IkvmTypeRef;
import consulo.internal.org.objectweb.asm.ClassWriter;
import consulo.internal.org.objectweb.asm.Opcodes;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.BitUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2014-05-09
 */
public class JavaMethodStubBuilder extends BaseStubBuilder<PsiMethod>
{
	private DotNetTypeRef myReturnType;
	private List<JavaParameterStubBuilder> myParameters = new ArrayList<JavaParameterStubBuilder>(5);
	private boolean myConstructor;

	public JavaMethodStubBuilder(PsiElement navTarget, String name, boolean constructor)
	{
		super(navTarget, name);
		myConstructor = constructor;
	}

	@NotNull
	@Override
	public PsiMethod buildToPsi(@Nullable PsiElement parent)
	{
		PsiManager psiManager = PsiManager.getInstance(myNavTarget.getProject());
		LightMethodBuilder builder = new LightMethodBuilder(psiManager, myName);
		builder.setConstructor(myConstructor);
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

	@RequiredReadAction
	@Override
	public void buildToText(@NotNull StringBuilder builder, BaseStubBuilder<?> parent)
	{
		for(String modifier : myModifiers)
		{
			builder.append(modifier).append(" ");
		}

		if(myConstructor)
		{
			builder.append(parent.getName());
		}
		else
		{
			builder.append(normalizeTypeText(myReturnType)).append(" ").append(normalize(myName));
		}
		builder.append("(");
		for(int i = 0; i < myParameters.size(); i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}
			JavaParameterStubBuilder javaParameterStubBuilder = myParameters.get(i);
			javaParameterStubBuilder.buildToText(builder, JavaMethodStubBuilder.this);
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

	@RequiredReadAction
	private void appendType(DotNetTypeRef typeRef, StringBuilder builder)
	{
		PsiElement element = typeRef.resolve().getElement();
		String vmQName = element instanceof DotNetTypeDeclaration ? ((DotNetTypeDeclaration) element).getVmQName() : null;
		if(vmQName == null)
		{
			builder.append("Ljava/lang/Object;");
			return;
		}

		if(DotNetTypes.System.Void.equals(vmQName))
		{
			builder.append("V");
		}
		else if(DotNetTypes.System.Int32.equals(vmQName))
		{
			builder.append("I");
		}
		else if(DotNetTypes.System.String.equals(vmQName))
		{
			appendType(new IkvmTypeRef(myNavTarget, CommonClassNames.JAVA_LANG_STRING), builder);
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
			if(vmQName.equals(DotNetTypes.System.Object))
			{
				appendType(new IkvmTypeRef(myNavTarget, CommonClassNames.JAVA_LANG_OBJECT), builder);
				return;
			}
			if(!vmQName.startsWith("java"))
			{
				vmQName = "cli." + vmQName;
			}
			builder.append("L").append(vmQName.replace(".", "/")).append(";");
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
