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

package org.mustbe.consulo.ikvm.psi;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.*;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;

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

	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return StringUtil.getPackageName(getPresentableQName());
	}

	@Nullable
	@Override
	public String getPresentableQName()
	{
		return myPsiClass.getQualifiedName();
	}

	@Override
	public String toString()
	{
		return "LightIkvmType: " + getPresentableQName();
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

	@Override
	public boolean isInheritor(@NotNull DotNetTypeDeclaration typeDeclaration, boolean b)
	{
		return false;
	}

	@Override
	public DotNetTypeRef getTypeRefForEnumConstants()
	{
		return null;
	}

	@Nullable
	@Override
	public String getVmQName()
	{
		return null;
	}

	@Nullable
	@Override
	public String getVmName()
	{
		return null;
	}

	@Nullable
	@Override
	public DotNetFieldDeclaration findFieldByName(@NotNull String s, boolean b)
	{
		return null;
	}

	@Override
	public void processConstructors(@NotNull Processor<DotNetConstructorDeclaration> dotNetConstructorDeclarationProcessor)
	{

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

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		if(modifier == DotNetModifier.PUBLIC)
		{
			return myPsiClass.hasModifierProperty(PsiModifier.PUBLIC);
		}
		else if(modifier == DotNetModifier.PRIVATE)
		{
			return myPsiClass.hasModifierProperty(PsiModifier.PRIVATE);
		}
		else if(modifier == DotNetModifier.PROTECTED)
		{
			return myPsiClass.hasModifierProperty(PsiModifier.PROTECTED);
		}
		return false;
	}

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
