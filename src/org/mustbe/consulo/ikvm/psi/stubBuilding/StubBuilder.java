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
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.dotnet.psi.DotNetFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetPropertyDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import com.intellij.psi.PsiModifier;

/**
 * @author VISTALL
 * @since 06.05.14
 */
public class StubBuilder
{
	@Nullable
	public static JavaClassStubBuilder build(@NotNull DotNetTypeDeclaration typeDeclaration)
	{
		String name = typeDeclaration.getName();
		if(name.contains("$"))
		{
			return null;
		}

		String packageName = typeDeclaration.getPresentableParentQName();
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
				if(accessors.length == 1 && accessors[0].getAccessorType() == CSharpSoftTokens.GET_KEYWORD)
				{
					DotNetType type = ((DotNetPropertyDeclaration) dotNetNamedElement).getType();
					if(type == null)
					{
						continue;
					}
					JavaFieldStubBuilder field = javaClassStubBuilder.field(dotNetNamedElement.getName(), dotNetNamedElement);
					copyModifiers((DotNetModifierListOwner) dotNetNamedElement, field);
					field.withType(type.toTypeRef());
				}
				else
				{
					//TODO [VISTALL] method
				}
			}
			else if(dotNetNamedElement instanceof DotNetFieldDeclaration)
			{
				DotNetType type = ((DotNetFieldDeclaration) dotNetNamedElement).getType();
				if(type == null)
				{
					continue;
				}
				JavaFieldStubBuilder field = javaClassStubBuilder.field(dotNetNamedElement.getName(), dotNetNamedElement);
				copyModifiers((DotNetModifierListOwner) dotNetNamedElement, field);
				field.withType(type.toTypeRef());
			}
		}
		return javaClassStubBuilder;
	}

	private static void copyModifiers(DotNetModifierListOwner modifierListOwner, BaseStubBuilder baseStubBuilder)
	{
		if(modifierListOwner.hasModifier(CSharpModifier.STATIC))
		{
			baseStubBuilder.addModifier(PsiModifier.STATIC);
		}

		if(modifierListOwner.hasModifier(CSharpModifier.PUBLIC))
		{
			baseStubBuilder.addModifier(PsiModifier.PUBLIC);
		}

		if(modifierListOwner.hasModifier(CSharpModifier.ABSTRACT))
		{
			baseStubBuilder.addModifier(PsiModifier.ABSTRACT);
		}
	}
}
