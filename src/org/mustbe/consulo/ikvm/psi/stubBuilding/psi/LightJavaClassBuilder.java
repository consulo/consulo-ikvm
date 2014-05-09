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

package org.mustbe.consulo.ikvm.psi.stubBuilding.psi;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.InheritanceImplUtil;
import com.intellij.psi.impl.PsiClassImplUtil;
import com.intellij.psi.impl.PsiSuperMethodImplUtil;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.impl.light.LightModifierList;
import com.intellij.psi.impl.source.ClassInnerStuffCache;
import com.intellij.psi.impl.source.PsiExtensibleClass;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 06.05.14
 */
public class LightJavaClassBuilder extends LightElement implements PsiExtensibleClass
{
	private PsiModifierList myModifierList;
	private String myName;
	private String myPackage;

	private List<PsiField> myFields = Collections.emptyList();
	private List<PsiMethod> myMethods = Collections.emptyList();
	private final ClassInnerStuffCache myInnersCache = new ClassInnerStuffCache(this);

	public LightJavaClassBuilder(@NotNull Project project)
	{
		super(PsiManager.getInstance(project), JavaLanguage.INSTANCE);
	}

	@Override
	public String toString()
	{
		return "PsiClass: " + getQualifiedName();
	}

	@Nullable
	@Override
	public String getQualifiedName()
	{
		if(StringUtil.isEmpty(myPackage))
		{
			return myName;
		}
		return myPackage + "." + myName;
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

	@Nullable
	@Override
	public PsiReferenceList getExtendsList()
	{
		return null;
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
		return PsiClassImplUtil.getSuperClass(this);
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
		return myInnersCache.getFields();
	}

	@NotNull
	@Override
	public PsiMethod[] getMethods()
	{
		return myInnersCache.getMethods();
	}

	@NotNull
	@Override
	public PsiMethod[] getConstructors()
	{
		return myInnersCache.getConstructors();
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
		return PsiClassImplUtil.getAllFields(this);
	}

	@NotNull
	@Override
	public PsiMethod[] getAllMethods()
	{
		return PsiClassImplUtil.getAllMethods(this);
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
		return myInnersCache.findFieldByName(s, b);
	}

	@Nullable
	@Override
	public PsiMethod findMethodBySignature(PsiMethod method, boolean b)
	{
		return PsiClassImplUtil.findMethodBySignature(this, method, b);
	}

	@NotNull
	@Override
	public PsiMethod[] findMethodsBySignature(PsiMethod method, boolean b)
	{
		return PsiClassImplUtil.findMethodsBySignature(this, method, b);
	}

	@NotNull
	@Override
	public PsiMethod[] findMethodsByName(@NonNls String s, boolean b)
	{
		return PsiClassImplUtil.findMethodsByName(this, s, b);
	}

	@Override
	@NotNull
	public List<Pair<PsiMethod, PsiSubstitutor>> findMethodsAndTheirSubstitutorsByName(String name, boolean checkBases)
	{
		return PsiClassImplUtil.findMethodsAndTheirSubstitutorsByName(this, name, checkBases);
	}

	@Override
	@NotNull
	public List<Pair<PsiMethod, PsiSubstitutor>> getAllMethodsAndTheirSubstitutors()
	{
		return PsiClassImplUtil.getAllWithSubstitutorsByMap(this, PsiClassImplUtil.MemberType.METHOD);
	}

	@Nullable
	@Override
	public PsiClass findInnerClassByName(@NonNls String s, boolean b)
	{
		return null;
	}

	@Override
	public String getName()
	{
		return myName;
	}

	@Override
	public PsiFile getContainingFile()
	{
		return getNavigationElement().getContainingFile();
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
		return getParent();
	}

	@Override
	public boolean isInheritor(@NotNull PsiClass baseClass, boolean checkDeep)
	{
		return InheritanceImplUtil.isInheritor(this, baseClass, checkDeep);
	}

	@Override
	public void accept(@NotNull PsiElementVisitor visitor)
	{
		if(visitor instanceof JavaElementVisitor)
		{
			((JavaElementVisitor) visitor).visitClass(this);
		}
		else
		{
			visitor.visitElement(this);
		}
	}

	@Override
	public boolean processDeclarations(
			@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place)
	{
		return PsiClassImplUtil.processDeclarationsInClass(this, processor, state, null, lastParent, place, false);
	}

	@Override
	public boolean isInheritorDeep(PsiClass baseClass, PsiClass classToByPass)
	{
		return InheritanceImplUtil.isInheritorDeep(this, baseClass, classToByPass);
	}

	@Override
	public ItemPresentation getPresentation()
	{
		return ItemPresentationProviders.getItemPresentation(this);
	}

	@Override
	public boolean isEquivalentTo(final PsiElement another)
	{
		return PsiClassImplUtil.isClassEquivalentTo(this, another);
	}

	@Override
	@NotNull
	public SearchScope getUseScope()
	{
		return PsiClassImplUtil.getClassUseScope(this);
	}

	@Nullable
	@Override
	public PsiClass getContainingClass()
	{
		return null;
	}

	@Override
	@NotNull
	public Collection<HierarchicalMethodSignature> getVisibleSignatures()
	{
		return PsiSuperMethodImplUtil.getVisibleSignatures(this);
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return null;
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
		return myModifierList;
	}

	@Override
	public boolean hasModifierProperty(@PsiModifier.ModifierConstant @NonNls @NotNull String s)
	{
		PsiModifierList modifierList = getModifierList();
		return modifierList != null && modifierList.hasModifierProperty(s);
	}

	public void withModifiers(@NotNull String... modifiers)
	{
		myModifierList = new LightModifierList(getManager(), JavaLanguage.INSTANCE, modifiers);
	}

	public void withName(String name)
	{
		myName = name;
	}

	public void withPackage(String packageName)
	{
		myPackage = packageName;
	}

	public void withFields(@NotNull List<PsiField> fields)
	{
		myFields = fields;
	}

	public void withMethods(@NotNull List<PsiMethod> methods)
	{
		myMethods = methods;
	}

	@NotNull
	@Override
	public List<PsiField> getOwnFields()
	{
		return myFields;
	}

	@NotNull
	@Override
	public List<PsiMethod> getOwnMethods()
	{
		return myMethods;
	}

	@NotNull
	@Override
	public List<PsiClass> getOwnInnerClasses()
	{
		return Collections.emptyList();
	}
}
