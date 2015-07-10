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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.psi.DotNetAttribute;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeUtil;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.ikvm.IKvmAttributes;
import org.mustbe.consulo.ikvm.psi.stubBuilding.BaseStubBuilder;
import org.mustbe.consulo.ikvm.psi.stubBuilding.JavaFieldStubBuilder;
import org.mustbe.consulo.ikvm.psi.stubBuilding.JavaMethodStubBuilder;
import org.mustbe.consulo.ikvm.psi.stubBuilding.StubBuilder;
import org.mustbe.consulo.msil.lang.psi.MsilClassEntry;
import org.mustbe.consulo.msil.lang.psi.MsilCustomAttribute;
import org.mustbe.consulo.msil.lang.stubbing.MsilCustomAttributeArgumentList;
import org.mustbe.consulo.msil.lang.stubbing.MsilCustomAttributeStubber;
import org.mustbe.consulo.msil.lang.stubbing.values.MsiCustomAttributeValue;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.openapi.util.AtomicNullableLazyValue;
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
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import sun.reflect.generics.parser.SignatureParser;
import sun.reflect.generics.tree.ClassSignature;
import sun.reflect.generics.tree.ClassTypeSignature;
import sun.reflect.generics.tree.FormalTypeParameter;
import sun.reflect.generics.tree.SimpleClassTypeSignature;

/**
 * @author VISTALL
 * @since 06.05.14
 */
public class DotNetTypeToJavaClass extends LightElement implements PsiExtensibleClass
{
	private PsiModifierList myModifierList;
	private String myName;
	private String myPackage;

	private List<PsiField> myFields = Collections.emptyList();
	private List<PsiMethod> myMethods = Collections.emptyList();
	private final ClassInnerStuffCache myInnersCache = new ClassInnerStuffCache(this);
	private DotNetTypeDeclaration myTypeDeclaration;
	private AtomicBoolean myInitMembers = new AtomicBoolean();

	private AtomicNullableLazyValue<ClassSignature> mySignatureValue = new AtomicNullableLazyValue<ClassSignature>()
	{
		@Nullable
		@Override
		@RequiredReadAction
		protected ClassSignature compute()
		{
			DotNetAttribute attribute = DotNetAttributeUtil.findAttribute(myTypeDeclaration, IKvmAttributes.SignatureAttribute);
			if(attribute instanceof MsilCustomAttribute)
			{
				MsilCustomAttributeArgumentList customAttributeArgumentList = MsilCustomAttributeStubber.build((MsilCustomAttribute) attribute);
				List<MsiCustomAttributeValue> constructorArguments = customAttributeArgumentList.getConstructorArguments();
				if(constructorArguments.size() != 1)
				{
					return null;
				}
				Object value = constructorArguments.get(0).getValue();
				if(!(value instanceof String))
				{
					return null;
				}
				SignatureParser make = SignatureParser.make();
				return make.parseClassSig((String) value);
			}
			return null;
		}
	};

	private ReentrantLock myLock = new ReentrantLock();

	public DotNetTypeToJavaClass(@NotNull DotNetTypeDeclaration typeDeclaration)
	{
		super(typeDeclaration.getManager(), JavaLanguage.INSTANCE);
		myTypeDeclaration = typeDeclaration;
	}

	@RequiredReadAction
	private void initMembers()
	{
		if(myInitMembers.get())
		{
			return;
		}

		myLock.lock();
		try
		{
			final List<PsiField> fields = new LinkedList<PsiField>();
			final List<PsiMethod> methods = new LinkedList<PsiMethod>();
			StubBuilder.processMembers(myTypeDeclaration, new Consumer<BaseStubBuilder<?>>()
			{
				@Override
				public void consume(BaseStubBuilder<?> member)
				{
					if(member instanceof JavaFieldStubBuilder)
					{
						fields.add((PsiField) member.buildToPsi(DotNetTypeToJavaClass.this));
					}
					else if(member instanceof JavaMethodStubBuilder)
					{
						methods.add((PsiMethod) member.buildToPsi(DotNetTypeToJavaClass.this));
					}
				}
			});
			myFields = fields;
			myMethods = methods;
			myInitMembers.set(true);

		}
		finally
		{
			myLock.unlock();
		}
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
		return myTypeDeclaration.isInterface();
	}

	@Override
	public boolean isAnnotationType()
	{
		return false;
	}

	@Override
	public boolean isEnum()
	{
		return myTypeDeclaration.isEnum();
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
	//@LazyInstance
	public PsiClassType[] getExtendsListTypes()
	{
		ClassSignature value = mySignatureValue.getValue();
		if(value != null)
		{
			ClassTypeSignature superclass = value.getSuperclass();
			if(superclass != null)
			{
				String join = StringUtil.join(superclass.getPath(), new Function<SimpleClassTypeSignature, String>()
				{
					@Override
					public String fun(SimpleClassTypeSignature simpleClassTypeSignature)
					{
						return simpleClassTypeSignature.getName();
					}
				}, ".");
				return new PsiClassType[]{(PsiClassType) JavaPsiFacade.getInstance(getProject()).getParserFacade().createTypeFromText(join, null)};
			}
		}
		return PsiClassType.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	//@LazyInstance
	public PsiClassType[] getImplementsListTypes()
	{
		ClassSignature value = mySignatureValue.getValue();
		if(value != null)
		{
			ClassTypeSignature[] superInterfaces = value.getSuperInterfaces();
			if(superInterfaces.length == 0)
			{
				return PsiClassType.EMPTY_ARRAY;
			}

			PsiClassType[] types = new PsiClassType[superInterfaces.length];
			for(int i = 0; i < superInterfaces.length; i++)
			{
				ClassTypeSignature superInterface = superInterfaces[i];
				String join = StringUtil.join(superInterface.getPath(), new Function<SimpleClassTypeSignature, String>()
				{
					@Override
					public String fun(SimpleClassTypeSignature simpleClassTypeSignature)
					{
						return simpleClassTypeSignature.getName();
					}
				}, ".");
				types[i] = (PsiClassType) JavaPsiFacade.getInstance(getProject()).getParserFacade().createTypeFromText(join, null);
			}
			return types;
		}

		return PsiClassType.EMPTY_ARRAY;
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
		return PsiClassImplUtil.getInterfaces(this);
	}

	@NotNull
	@Override
	public PsiClass[] getSupers()
	{
		return PsiClassImplUtil.getSupers(this);
	}

	@NotNull
	@Override
	public PsiClassType[] getSuperTypes()
	{
		return PsiClassImplUtil.getSuperTypes(this);
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
	public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
			@NotNull ResolveState state,
			PsiElement lastParent,
			@NotNull PsiElement place)
	{
		return PsiClassImplUtil.processDeclarationsInClass(this, processor, state, null, lastParent, place, PsiUtil.getLanguageLevel(place), false);
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
	@RequiredReadAction
	public boolean hasTypeParameters()
	{
		return getTypeParameters().length > 0;
	}

	@Nullable
	@Override
	public PsiTypeParameterList getTypeParameterList()
	{
		return null;
	}

	@NotNull
	@Override
	@RequiredReadAction
	@LazyInstance
	public PsiTypeParameter[] getTypeParameters()
	{
		DotNetGenericParameter[] genericParameters = myTypeDeclaration.getGenericParameters();
		if(genericParameters.length > 0)
		{
			PsiTypeParameter[] typeParameters = new PsiTypeParameter[genericParameters.length];
			for(int i = 0; i < genericParameters.length; i++)
			{
				DotNetGenericParameter genericParameter = genericParameters[i];
				typeParameters[i] = new DotNetGenericParameterBuilder(DotNetTypeToJavaClass.this, genericParameter.getName(), i);
			}
			return typeParameters;
		}
		else if(myTypeDeclaration instanceof MsilClassEntry)
		{
			ClassSignature classSignature = mySignatureValue.getValue();
			if(classSignature == null)
			{
				return PsiTypeParameter.EMPTY_ARRAY;
			}
			FormalTypeParameter[] formalTypeParameters = classSignature.getFormalTypeParameters();
			if(formalTypeParameters.length == 0)
			{
				return PsiTypeParameter.EMPTY_ARRAY;
			}

			PsiTypeParameter[] typeParameters = new PsiTypeParameter[formalTypeParameters.length];
			for(int i = 0; i < formalTypeParameters.length; i++)
			{
				FormalTypeParameter formalTypeParameter = formalTypeParameters[i];
				typeParameters[i] = new DotNetGenericParameterBuilder(DotNetTypeToJavaClass.this, formalTypeParameter.getName(), i);
			}
			return typeParameters;
		}
		return PsiTypeParameter.EMPTY_ARRAY;
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

	@NotNull
	@Override
	@RequiredReadAction
	public List<PsiField> getOwnFields()
	{
		initMembers();
		return myFields;
	}

	@NotNull
	@Override
	@RequiredReadAction
	public List<PsiMethod> getOwnMethods()
	{
		initMembers();
		return myMethods;
	}

	@NotNull
	@Override
	public List<PsiClass> getOwnInnerClasses()
	{
		return Collections.emptyList();
	}
}
