/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.ikvm.psi.stubBuilding.psi;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.impl.light.LightReferenceListBuilder;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 10.07.2015
 */
public class DotNetGenericParameterBuilder extends LightElement implements PsiTypeParameter
{
	private PsiTypeParameterListOwner myOwner;
	private String myName;
	private int myIndex;

	public DotNetGenericParameterBuilder(@NotNull PsiTypeParameterListOwner owner, @NotNull String name, int index)
	{
		super(owner.getManager(), JavaLanguage.INSTANCE);
		myOwner = owner;
		myName = name;
		myIndex = index;
	}

	@Override
	public String getName()
	{
		return myName;
	}

	@Override
	public String toString()
	{
		return null;
	}

	@Nullable
	@Override
	public String getQualifiedName()
	{
		return null;
	}

	@Override
	public boolean isInterface()
	{
		return false;
	}

	@Override
	public boolean isAnnotationType()
	{
		return false;
	}

	@Override
	public boolean isEnum()
	{
		return false;
	}

	@NotNull
	@Override
	public PsiReferenceList getExtendsList()
	{
		return new LightReferenceListBuilder(getManager(), PsiReferenceList.Role.EXTENDS_LIST);
	}

	@Nullable
	@Override
	public PsiReferenceList getImplementsList()
	{
		return null;
	}

	@NotNull
	@Override
	public PsiClassType[] getExtendsListTypes()
	{
		return new PsiClassType[0];
	}

	@NotNull
	@Override
	public PsiClassType[] getImplementsListTypes()
	{
		return new PsiClassType[0];
	}

	@Nullable
	@Override
	public PsiClass getSuperClass()
	{
		return null;
	}

	@Override
	public PsiClass[] getInterfaces()
	{
		return new PsiClass[0];
	}

	@NotNull
	@Override
	public PsiClass[] getSupers()
	{
		return new PsiClass[0];
	}

	@NotNull
	@Override
	public PsiClassType[] getSuperTypes()
	{
		return new PsiClassType[0];
	}

	@NotNull
	@Override
	public PsiField[] getFields()
	{
		return new PsiField[0];
	}

	@NotNull
	@Override
	public PsiMethod[] getMethods()
	{
		return new PsiMethod[0];
	}

	@NotNull
	@Override
	public PsiMethod[] getConstructors()
	{
		return new PsiMethod[0];
	}

	@NotNull
	@Override
	public PsiClass[] getInnerClasses()
	{
		return new PsiClass[0];
	}

	@NotNull
	@Override
	public PsiClassInitializer[] getInitializers()
	{
		return new PsiClassInitializer[0];
	}

	@NotNull
	@Override
	public PsiField[] getAllFields()
	{
		return new PsiField[0];
	}

	@NotNull
	@Override
	public PsiMethod[] getAllMethods()
	{
		return new PsiMethod[0];
	}

	@NotNull
	@Override
	public PsiClass[] getAllInnerClasses()
	{
		return new PsiClass[0];
	}

	@Nullable
	@Override
	public PsiField findFieldByName(@NonNls String s, boolean b)
	{
		return null;
	}

	@Nullable
	@Override
	public PsiMethod findMethodBySignature(PsiMethod psiMethod, boolean b)
	{
		return null;
	}

	@NotNull
	@Override
	public PsiMethod[] findMethodsBySignature(PsiMethod psiMethod, boolean b)
	{
		return new PsiMethod[0];
	}

	@NotNull
	@Override
	public PsiMethod[] findMethodsByName(@NonNls String s, boolean b)
	{
		return new PsiMethod[0];
	}

	@NotNull
	@Override
	public List<Pair<PsiMethod, PsiSubstitutor>> findMethodsAndTheirSubstitutorsByName(@NonNls String s, boolean b)
	{
		return Collections.emptyList();
	}

	@NotNull
	@Override
	public List<Pair<PsiMethod, PsiSubstitutor>> getAllMethodsAndTheirSubstitutors()
	{
		return Collections.emptyList();
	}

	@Nullable
	@Override
	public PsiClass findInnerClassByName(@NonNls String s, boolean b)
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement getLBrace()
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement getRBrace()
	{
		return null;
	}

	@Nullable
	@Override
	public PsiIdentifier getNameIdentifier()
	{
		return null;
	}

	@Override
	public PsiElement getScope()
	{
		return null;
	}

	@Override
	public boolean isInheritor(@NotNull PsiClass psiClass, boolean b)
	{
		return false;
	}

	@Override
	public boolean isInheritorDeep(PsiClass psiClass, @Nullable PsiClass psiClass2)
	{
		return false;
	}

	@Nullable
	@Override
	public PsiClass getContainingClass()
	{
		return null;
	}

	@NotNull
	@Override
	public Collection<HierarchicalMethodSignature> getVisibleSignatures()
	{
		return Collections.emptyList();
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return null;
	}

	@Nullable
	@Override
	public PsiTypeParameterListOwner getOwner()
	{
		return myOwner;
	}

	@Override
	public int getIndex()
	{
		return myIndex;
	}

	@NotNull
	@Override
	public PsiAnnotation[] getAnnotations()
	{
		return new PsiAnnotation[0];
	}

	@NotNull
	@Override
	public PsiAnnotation[] getApplicableAnnotations()
	{
		return new PsiAnnotation[0];
	}

	@Nullable
	@Override
	public PsiAnnotation findAnnotation(@NotNull @NonNls String s)
	{
		return null;
	}

	@NotNull
	@Override
	public PsiAnnotation addAnnotation(@NotNull @NonNls String s)
	{
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public PsiDocComment getDocComment()
	{
		return null;
	}

	@Override
	public boolean isDeprecated()
	{
		return false;
	}

	@Override
	public boolean hasTypeParameters()
	{
		return false;
	}

	@Nullable
	@Override
	public PsiTypeParameterList getTypeParameterList()
	{
		return null;
	}

	@NotNull
	@Override
	public PsiTypeParameter[] getTypeParameters()
	{
		return new PsiTypeParameter[0];
	}

	@Nullable
	@Override
	public PsiModifierList getModifierList()
	{
		return null;
	}

	@Override
	public boolean hasModifierProperty(@PsiModifier.ModifierConstant @NonNls @NotNull String s)
	{
		return false;
	}
}
