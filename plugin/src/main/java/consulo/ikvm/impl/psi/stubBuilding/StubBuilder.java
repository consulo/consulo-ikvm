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

import com.intellij.java.language.psi.PsiModifier;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.progress.ProgressManager;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.ikvm.module.extension.IkvmModuleExtension;
import consulo.internal.dotnet.msil.decompiler.util.MsilHelper;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.PsiUtilCore;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.layer.orderEntry.ModuleExtensionWithSdkOrderEntry;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.module.extension.ModuleExtensionWithSdk;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.archive.ArchiveVfsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 06.05.14
 */
public class StubBuilder
{
	@RequiredReadAction
	private static boolean isIKVMLibrary(@NotNull DotNetTypeDeclaration typeDeclaration)
	{
		VirtualFile virtualFile = PsiUtilCore.getVirtualFile(typeDeclaration);
		if(virtualFile == null)
		{
			return false;
		}
		VirtualFile virtualFileForArchive = ArchiveVfsUtil.getVirtualFileForArchive(virtualFile);
		if(virtualFileForArchive == null)
		{
			return false;
		}

		List<OrderEntry> orderEntriesForFile = ProjectFileIndex.SERVICE.getInstance(typeDeclaration.getProject()).getOrderEntriesForFile
				(virtualFile);
		if(orderEntriesForFile.isEmpty())
		{
			return false;
		}
		for(OrderEntry orderEntry : orderEntriesForFile)
		{
			if(orderEntry instanceof ModuleExtensionWithSdkOrderEntry)
			{
				ModuleExtensionWithSdk<?> moduleExtension = ((ModuleExtensionWithSdkOrderEntry) orderEntry).getModuleExtension();
				if(moduleExtension instanceof IkvmModuleExtension)
				{
					return true;
				}
			}
		}
		return false;
	}

	@Nullable
	@RequiredReadAction
	public static JavaClassStubBuilder build(@NotNull DotNetTypeDeclaration typeDeclaration)
	{
		String name = typeDeclaration.getName();
		if(name == null)
		{
			return null;
		}
		if(name.contains("$") || typeDeclaration.isNested())
		{
			return null;
		}

		name = MsilHelper.cutGenericMarker(name);

		String packageName = typeDeclaration.getPresentableParentQName();
		if(!isIKVMLibrary(typeDeclaration))
		{
			packageName = StringUtil.isEmpty(packageName) ? "cli" : "cli." + packageName;
		}

		JavaClassStubBuilder javaClassStubBuilder = new JavaClassStubBuilder(packageName, name, typeDeclaration);

		copyModifiers(typeDeclaration, javaClassStubBuilder);

		return javaClassStubBuilder;
	}

	@RequiredReadAction
	public static void processMembers(DotNetTypeDeclaration typeDeclaration, Consumer<BaseStubBuilder<?>> consumer)
	{
		for(DotNetNamedElement dotNetNamedElement : typeDeclaration.getMembers())
		{
			ProgressManager.checkCanceled();

			if(dotNetNamedElement instanceof DotNetPropertyDeclaration)
			{
				DotNetXAccessor[] accessors = ((DotNetPropertyDeclaration) dotNetNamedElement).getAccessors();
				// field
				if(accessors.length == 1 && accessors[0].getAccessorKind() == DotNetXAccessor.Kind.GET)
				{
					DotNetTypeRef typeRef = ((DotNetPropertyDeclaration) dotNetNamedElement).toTypeRef(false);
					if(typeRef == DotNetTypeRef.ERROR_TYPE)
					{
						continue;
					}
					JavaFieldStubBuilder field = new JavaFieldStubBuilder(dotNetNamedElement, dotNetNamedElement.getName());
					copyModifiers((DotNetModifierListOwner) dotNetNamedElement, field);
					field.withType(typeRef);

					consumer.accept(field);
				}
				else
				{
					//TODO [VISTALL] method
				}
			}
			else if(dotNetNamedElement instanceof DotNetFieldDeclaration)
			{
				if(isSkipped(dotNetNamedElement))
				{
					continue;
				}

				DotNetTypeRef typeRef = ((DotNetFieldDeclaration) dotNetNamedElement).toTypeRef(false);
				if(typeRef == DotNetTypeRef.ERROR_TYPE)
				{
					continue;
				}
				JavaFieldStubBuilder field = new JavaFieldStubBuilder(dotNetNamedElement, dotNetNamedElement.getName());
				copyModifiers((DotNetModifierListOwner) dotNetNamedElement, field);
				field.withType(typeRef);
				consumer.accept(field);
			}
			else if(dotNetNamedElement instanceof DotNetMethodDeclaration)
			{
				if(isSkipped(dotNetNamedElement))
				{
					continue;
				}

				// TODO [VISTALL] intro getVmName() it ill return always '.ctor' for constructors, due in C# it will return class name
				String name = dotNetNamedElement.getName();
				// skip static constructors
				if(MsilHelper.STATIC_CONSTRUCTOR_NAME.equals(name))
				{
					continue;
				}

				boolean constructor = MsilHelper.CONSTRUCTOR_NAME.equals(name);

				JavaMethodStubBuilder method = new JavaMethodStubBuilder(dotNetNamedElement, name, constructor);
				copyModifiers((DotNetModifierListOwner) dotNetNamedElement, method);
				method.withReturnType(((DotNetMethodDeclaration) dotNetNamedElement).getReturnTypeRef());

				DotNetParameter[] parameters = ((DotNetMethodDeclaration) dotNetNamedElement).getParameters();
				for(int i = 0; i < parameters.length; i++)
				{
					DotNetParameter parameter = parameters[i];
					String paramName = parameter.getName();
					method.withParameter(parameter.toTypeRef(false), StringUtil.isEmpty(paramName) ? "p" + i : paramName, parameter);
				}

				consumer.accept(method);
			}
		}
	}

	private static boolean isSkipped(PsiNamedElement element)
	{
		String name = element.getName();
		if(name == null)
		{
			return true;
		}
		for(int i = 0; i < name.length(); i++)
		{
			char c = name.charAt(i);
			if(c == ':' || c == '<')
			{
				return true;
			}
		}
		return false;
	}

	@RequiredReadAction
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
			//baseStubBuilder.addModifier(PsiModifier.PACKAGE_LOCAL);
		}

		if(modifierListOwner.hasModifier(DotNetModifier.ABSTRACT))
		{
			baseStubBuilder.addModifier(PsiModifier.ABSTRACT);
		}
	}
}
