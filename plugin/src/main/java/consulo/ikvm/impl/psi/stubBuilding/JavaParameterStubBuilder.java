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

import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.impl.psi.impl.light.LightParameter;
import com.intellij.java.language.psi.PsiParameter;
import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.internal.org.objectweb.asm.ClassWriter;
import consulo.language.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 09.05.14
 */
public class JavaParameterStubBuilder extends BaseStubBuilder<PsiParameter>
{
	private DotNetTypeRef myType;

	public JavaParameterStubBuilder(PsiElement navTarget, String name)
	{
		super(navTarget, name);
	}

	@NotNull
	@Override
	public PsiParameter buildToPsi(@Nullable PsiElement parent)
	{
		LightParameter parameter = new LightParameter(myName, normalizeType(myType), parent, JavaLanguage.INSTANCE);
		parameter.setNavigationElement(myNavTarget);
		return parameter;
	}

	@RequiredReadAction
	@Override
	public void buildToText(@NotNull StringBuilder builder, BaseStubBuilder<?> parent)
	{
		for(String modifier : myModifiers)
		{
			builder.append(modifier).append(" ");
		}
		builder.append(normalizeTypeText(myType)).append(" ").append(normalize(myName));
	}

	@Override
	public void buildToBytecode(ClassWriter parent)
	{

	}

	public DotNetTypeRef getType()
	{
		return myType;
	}

	public JavaParameterStubBuilder withType(@NotNull DotNetTypeRef type)
	{
		myType = type;
		return this;
	}
}
