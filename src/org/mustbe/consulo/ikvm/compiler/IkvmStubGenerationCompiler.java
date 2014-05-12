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

package org.mustbe.consulo.ikvm.compiler;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerUtil;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.ikvm.bundle.IkvmBundleType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.compiler.SourceGeneratingCompiler;
import com.intellij.openapi.compiler.ValidityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.SdkOrderEntry;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.openapi.vfs.util.ArchiveVfsUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import lombok.val;

/**
 * @author VISTALL
 * @since 09.05.14
 */
public class IkvmStubGenerationCompiler implements SourceGeneratingCompiler
{
	@Override
	public VirtualFile getPresentableFile(
			CompileContext compileContext, Module module, VirtualFile virtualFile, VirtualFile virtualFile2)
	{
		return null;
	}

	@Override
	public GenerationItem[] getGenerationItems(CompileContext compileContext)
	{
		Module[] affectedModules = compileContext.getCompileScope().getAffectedModules();
		for(Module affectedModule : affectedModules)
		{
			Set<File> files = DotNetCompilerUtil.collectDependencies(affectedModule, true);
			for(File file : files)
			{
				List<DotNetTypeDeclaration> typeDeclarations = collectTypes(compileContext.getProject(), file);
				if(typeDeclarations.isEmpty())
				{
					continue;
				}
				System.out.println(typeDeclarations.size());
			}
		}
		return new GenerationItem[0];
	}

	private List<DotNetTypeDeclaration> collectTypes(Project project, File dllFile)
	{
		VirtualFile fileByIoFile = LocalFileSystem.getInstance().findFileByIoFile(dllFile);
		if(fileByIoFile == null)
		{
			return Collections.emptyList();
		}

		VirtualFile archiveRootForLocalFile = ArchiveVfsUtil.getArchiveRootForLocalFile(fileByIoFile);

		if(archiveRootForLocalFile == null)
		{
			return Collections.emptyList();
		}

		List<OrderEntry> orderEntriesForFile = ProjectFileIndex.SERVICE.getInstance(project).getOrderEntriesForFile(archiveRootForLocalFile);
		for(OrderEntry orderEntry : orderEntriesForFile)
		{
			if(orderEntry instanceof SdkOrderEntry)
			{
				Sdk sdk = ((SdkOrderEntry) orderEntry).getSdk();
				if(sdk == null)
				{
					continue;
				}
				if(sdk.getSdkType() instanceof IkvmBundleType)
				{
					return Collections.emptyList();
				}
			}
		}

		val psiManager = PsiManager.getInstance(project);
		val list = new ArrayList<DotNetTypeDeclaration>();
		VfsUtil.visitChildrenRecursively(archiveRootForLocalFile, new VirtualFileVisitor()
		{
			@Override
			public boolean visitFile(@NotNull final VirtualFile file)
			{
				ApplicationManager.getApplication().runReadAction(new Runnable()
				{
					@Override
					public void run()
					{
						PsiFile psiFile = psiManager.findFile(file);
						if(!(psiFile instanceof CSharpFileImpl))
						{
							return;
						}
						for(DotNetQualifiedElement qualifiedElement : ((CSharpFileImpl) psiFile).getMembers())
						{
							if(qualifiedElement instanceof DotNetTypeDeclaration)
							{
								list.add((DotNetTypeDeclaration) qualifiedElement);
							}
						}
					}
				});
				return true;
			}
		});
		return list;
	}

	@Override
	public GenerationItem[] generate(
			CompileContext compileContext, GenerationItem[] generationItems, VirtualFile virtualFile)
	{
		return new GenerationItem[0];
	}

	@NotNull
	@Override
	public String getDescription()
	{
		return "IkvmStubGenerationCompiler";
	}

	@Override
	public boolean validateConfiguration(CompileScope compileScope)
	{
		return true;
	}

	@Override
	public void init(@NotNull CompilerManager compilerManager)
	{

	}

	@Override
	public ValidityState createValidityState(DataInput dataInput) throws IOException
	{
		return null;
	}
}
