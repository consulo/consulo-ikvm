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

package org.mustbe.consulo.ikvm.psi.stubBuilding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.DotNetFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetPropertyDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiModifier;

/**
 * @author VISTALL
 * @since 06.05.14
 */
public class StubBuilder
{
	@Nullable
	public static JavaClassStubBuilder build(@NotNull DotNetTypeDeclaration typeDeclaration, boolean cli)
	{
		String name = typeDeclaration.getName();
		if(name.contains("$"))
		{
			return null;
		}

		String packageName = typeDeclaration.getPresentableParentQName();
		if(cli)
		{
			packageName = StringUtil.isEmpty(packageName) ? "cli" : "cli." + packageName;
		}
		/*
		String fileName = typeDeclaration.getContainingFile().getName();
		if(!fileName.startsWith("IKVM.OpenJDK."))
		{
			if(StringUtil.isEmpty(packageName))
			{
				packageName = "cli";
			}
			else
			{
				packageName = "cli." + packageName;
			}
		} */


		JavaClassStubBuilder javaClassStubBuilder = new JavaClassStubBuilder(packageName, name, typeDeclaration);

		copyModifiers(typeDeclaration, javaClassStubBuilder);

		for(DotNetNamedElement dotNetNamedElement : typeDeclaration.getMembers())
		{
			if(dotNetNamedElement instanceof DotNetPropertyDeclaration)
			{
				DotNetXXXAccessor[] accessors = ((DotNetPropertyDeclaration) dotNetNamedElement).getAccessors();
				// field
				if(accessors.length == 1 && accessors[0].getAccessorKind() == DotNetXXXAccessor.Kind.GET)
				{
					DotNetTypeRef typeRef = ((DotNetPropertyDeclaration) dotNetNamedElement).toTypeRef(false);
					if(typeRef == DotNetTypeRef.ERROR_TYPE)
					{
						continue;
					}
					JavaFieldStubBuilder field = javaClassStubBuilder.field(dotNetNamedElement.getName(), dotNetNamedElement);
					copyModifiers((DotNetModifierListOwner) dotNetNamedElement, field);
					field.withType(typeRef);
				}
				else
				{
					//TODO [VISTALL] method
				}
			}
			else if(dotNetNamedElement instanceof DotNetFieldDeclaration)
			{
				DotNetTypeRef typeRef = ((DotNetFieldDeclaration) dotNetNamedElement).toTypeRef(false);
				if(typeRef == DotNetTypeRef.ERROR_TYPE)
				{
					continue;
				}
				JavaFieldStubBuilder field = javaClassStubBuilder.field(dotNetNamedElement.getName(), dotNetNamedElement);
				copyModifiers((DotNetModifierListOwner) dotNetNamedElement, field);
				field.withType(typeRef);
			}
			else if(dotNetNamedElement instanceof DotNetMethodDeclaration)
			{
				JavaMethodStubBuilder method = javaClassStubBuilder.method(dotNetNamedElement.getName(), dotNetNamedElement);
				copyModifiers((DotNetModifierListOwner) dotNetNamedElement, method);
				method.withReturnType(((DotNetMethodDeclaration) dotNetNamedElement).getReturnTypeRef());

				DotNetParameter[] parameters = ((DotNetMethodDeclaration) dotNetNamedElement).getParameters();
				for(int i = 0; i < parameters.length; i++)
				{
					DotNetParameter parameter = parameters[i];
					String paramName = parameter.getName();
					method.withParameter(parameter.toTypeRef(false), paramName == null ? "p" + i : paramName, parameter);
				}
			}
		}

		return javaClassStubBuilder;
	}

	private static void copyModifiers(DotNetModifierListOwner modifierListOwner, BaseStubBuilder baseStubBuilder)
	{
		if(modifierListOwner.hasModifier(DotNetModifier.STATIC))
		{
			baseStubBuilder.addModifier(PsiModifier.STATIC);
		}

		if(modifierListOwner.hasModifier(DotNetModifier.PUBLIC))
		{
			baseStubBuilder.addModifier(PsiModifier.PUBLIC);
		}
		else if(modifierListOwner.hasModifier(DotNetModifier.PRIVATE))
		{
			baseStubBuilder.addModifier(PsiModifier.PRIVATE);
		}
		else if(modifierListOwner.hasModifier(DotNetModifier.PROTECTED))
		{
			baseStubBuilder.addModifier(PsiModifier.PROTECTED);
		}
		else
		{
			baseStubBuilder.addModifier(PsiModifier.PACKAGE_LOCAL);
		}

		if(modifierListOwner.hasModifier(DotNetModifier.ABSTRACT))
		{
			baseStubBuilder.addModifier(PsiModifier.ABSTRACT);
		}
	}
}
