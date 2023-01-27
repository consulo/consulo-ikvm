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

package consulo.ikvm.impl.psi.stubBuilding.psi;

import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.impl.psi.impl.InheritanceImplUtil;
import com.intellij.java.language.impl.psi.impl.PsiClassImplUtil;
import com.intellij.java.language.impl.psi.impl.PsiSuperMethodImplUtil;
import com.intellij.java.language.impl.psi.impl.light.LightModifierList;
import com.intellij.java.language.impl.psi.impl.source.ClassInnerStuffCache;
import com.intellij.java.language.impl.psi.impl.source.PsiExtensibleClass;
import com.intellij.java.language.impl.psi.impl.source.PsiImmediateClassType;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.javadoc.PsiDocComment;
import com.intellij.java.language.psi.util.PsiUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.AtomicNullableLazyValue;
import consulo.application.util.NotNullLazyValue;
import consulo.content.scope.SearchScope;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetAttributeUtil;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.ikvm.IKvmAttributes;
import consulo.ikvm.impl.$internal.ru.andrew.jclazz.core.signature.*;
import consulo.ikvm.impl.psi.stubBuilding.BaseStubBuilder;
import consulo.ikvm.impl.psi.stubBuilding.JavaFieldStubBuilder;
import consulo.ikvm.impl.psi.stubBuilding.JavaMethodStubBuilder;
import consulo.ikvm.impl.psi.stubBuilding.StubBuilder;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.util.IncorrectOperationException;
import consulo.msil.impl.lang.stubbing.MsilCustomAttributeArgumentList;
import consulo.msil.impl.lang.stubbing.MsilCustomAttributeStubber;
import consulo.msil.impl.lang.stubbing.values.MsiCustomAttributeValue;
import consulo.msil.lang.psi.MsilClassEntry;
import consulo.msil.lang.psi.MsilCustomAttribute;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

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
				return new ClassSignature((String) value);
			}
			return null;
		}
	};

	private NotNullLazyValue<PsiTypeParameter[]> myTypeParametersValue = NotNullLazyValue.createValue(() ->
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
			FormalTypeParameter[] formalTypeParameters = classSignature.getTypeParameters();
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
	});

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
				public void accept(BaseStubBuilder<?> member)
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
	@RequiredReadAction
	//@LazyInstance
	public PsiClassType[] getExtendsListTypes()
	{
		ClassSignature value = mySignatureValue.getValue();
		if(value != null)
		{
			ClassTypeSignature superclass = value.getSuperClass();
			if(superclass != null)
			{
				return new PsiClassType[]{(PsiClassType) convert(superclass)};
			}
		}
		return PsiClassType.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	@RequiredReadAction
	//@LazyInstance
	public PsiClassType[] getImplementsListTypes()
	{
		ClassSignature value = mySignatureValue.getValue();
		if(value != null)
		{
			ClassTypeSignature[] superInterfaces = value.getInterfaces();
			if(superInterfaces.length == 0)
			{
				return PsiClassType.EMPTY_ARRAY;
			}

			PsiClassType[] types = new PsiClassType[superInterfaces.length];
			for(int i = 0; i < superInterfaces.length; i++)
			{
				ClassTypeSignature superInterface = superInterfaces[i];
				types[i] = (PsiClassType) convert(superInterface);
			}
			return types;
		}

		return PsiClassType.EMPTY_ARRAY;
	}

	@NotNull
	@RequiredReadAction
	private PsiType convert(@NotNull ClassTypeSignature typeSignature)
	{
		String className = typeSignature.getClassName();
		SimpleClassTypeSignature classType = typeSignature.getClassType();
		TypeArgument[] typeArguments = classType.getTypeArguments();

		PsiType typeFromText = JavaPsiFacade.getInstance(getProject()).getParserFacade().createTypeFromText(className, myTypeDeclaration);
		if(typeFromText instanceof PsiClassType)
		{
			if(typeArguments.length > 0)
			{
				PsiClass resolved = ((PsiClassType) typeFromText).resolve();
				if(resolved == null)
				{
					return typeFromText;
				}

				PsiTypeParameter[] typeParameters = resolved.getTypeParameters();
				if(typeParameters.length != typeArguments.length)
				{
					return typeFromText;
				}

				PsiTypeParameter[] thisTypeParameters = getTypeParameters();
				Map<String, PsiTypeParameter> params = new HashMap<String, PsiTypeParameter>(thisTypeParameters.length);
				for(PsiTypeParameter psiTypeParameter : thisTypeParameters)
				{
					params.put(psiTypeParameter.getName(), psiTypeParameter);
				}

				PsiSubstitutor substitutor = PsiSubstitutor.EMPTY;

				for(int i = 0; i < typeParameters.length; i++)
				{
					PsiTypeParameter typeParameter = typeParameters[i];
					TypeArgument typeArgument = typeArguments[i];

					String typeArgumentVariableValue = typeArgument.getFieldType().getVariable();
					if(typeArgumentVariableValue != null)
					{
						PsiTypeParameter psiTypeParameter = params.get(typeArgumentVariableValue);
						assert psiTypeParameter != null;

						substitutor = substitutor.put(typeParameter, new PsiImmediateClassType(psiTypeParameter, PsiSubstitutor.EMPTY));
					}
					else
					{
						ClassTypeSignature typeArgumentClassType = typeArgument.getFieldType().getClassType();
						if(typeArgumentClassType != null)
						{
							substitutor = substitutor.put(typeParameter, convert(typeArgumentClassType));
						}
						else
						{
							throw new UnsupportedOperationException("Array");
						}
					}
				}
				return new PsiImmediateClassType(resolved, substitutor);
			}
		}
		return typeFromText;
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
		return PsiClass.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	public PsiClassInitializer[] getInitializers()
	{
		return PsiClassInitializer.EMPTY_ARRAY;
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
		return PsiClass.EMPTY_ARRAY;
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
	public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place)
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
		return ItemPresentationProvider.getItemPresentation(this);
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
	public PsiTypeParameter[] getTypeParameters()
	{
		return myTypeParametersValue.getValue();
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
