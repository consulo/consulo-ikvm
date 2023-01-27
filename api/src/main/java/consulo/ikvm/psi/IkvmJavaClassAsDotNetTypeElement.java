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

package consulo.ikvm.psi;

import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.util.lang.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 25.09.14
 */
public class IkvmJavaClassAsDotNetTypeElement extends LightElement implements DotNetTypeDeclaration
{
	private final PsiClass myPsiClass;

	public IkvmJavaClassAsDotNetTypeElement(PsiClass psiClass)
	{
		super(psiClass.getManager(), JavaLanguage.INSTANCE);
		myPsiClass = psiClass;
	}

	public PsiClass getPsiClass()
	{
		return myPsiClass;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return StringUtil.getPackageName(getPresentableQName());
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableQName()
	{
		return myPsiClass.getQualifiedName();
	}

	@Override
	public String toString()
	{
		return "IkvmJavaClassAsDotNetTypeElement: " + getPresentableQName();
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public boolean isInterface()
	{
		return myPsiClass.isInterface();
	}

	@Override
	public boolean isStruct()
	{
		return false;
	}

	@Override
	public boolean isEnum()
	{
		return myPsiClass.isEnum();
	}

	@Override
	public boolean isNested()
	{
		return false;
	}

	@Nullable
	@Override
	public DotNetTypeList getExtendList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		return new DotNetTypeRef[0];
	}

	@RequiredReadAction
	@Override
	public boolean isInheritor(@NotNull String otherVmQName, boolean b)
	{
		return false;
	}

	@Override
	public DotNetTypeRef getTypeRefForEnumConstants()
	{
		return DotNetTypeRef.AUTO_TYPE;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getVmQName()
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getVmName()
	{
		return null;
	}

	@Nullable
	@Override
	public DotNetGenericParameterList getGenericParameterList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetGenericParameter[] getGenericParameters()
	{
		return new DotNetGenericParameter[0];
	}

	@Override
	public int getGenericParametersCount()
	{
		return 0;
	}

	@NotNull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return new DotNetNamedElement[0];
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		return false;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}
}
