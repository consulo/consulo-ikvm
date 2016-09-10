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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.org.objectweb.asm.ClassWriter;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.impl.light.LightFieldBuilder;
import com.intellij.util.ArrayUtil;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 06.05.14
 */
public class JavaFieldStubBuilder extends BaseStubBuilder<PsiField>
{
	private DotNetTypeRef myType;

	public JavaFieldStubBuilder(PsiElement navTarget, String name)
	{
		super(navTarget, name);
	}

	@NotNull
	@Override
	public PsiField buildToPsi(@Nullable PsiElement parent)
	{
		LightFieldBuilder builder = new LightFieldBuilder(normalize(myName), normalizeType(myType), myNavTarget);
		builder.setModifiers(ArrayUtil.toStringArray(myModifiers));
		builder.setContainingClass((PsiClass) parent);
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
		builder.append(normalizeTypeText(myType)).append(" ").append(normalize(myName)).append(";");
	}

	@Override
	public void buildToBytecode(ClassWriter parent)
	{

	}

	public JavaFieldStubBuilder withType(@NotNull DotNetTypeRef type)
	{
		myType = type;
		return this;
	}
}
