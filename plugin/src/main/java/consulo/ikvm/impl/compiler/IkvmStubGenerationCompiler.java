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

package consulo.ikvm.impl.compiler;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.application.ReadAction;
import consulo.application.util.function.Computable;
import consulo.compiler.CompileContext;
import consulo.compiler.SourceProcessingCompiler;
import consulo.compiler.ValidityState;
import consulo.compiler.scope.CompileScope;
import consulo.container.boot.ContainerPathManager;
import consulo.content.bundle.Sdk;
import consulo.dotnet.DotNetTarget;
import consulo.dotnet.impl.compiler.DotNetCompilerUtil;
import consulo.dotnet.psi.DotNetMemberOwner;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.ikvm.bundle.IkvmBundleType;
import consulo.ikvm.impl.psi.stubBuilding.JavaClassStubBuilder;
import consulo.ikvm.impl.psi.stubBuilding.StubBuilder;
import consulo.ikvm.module.extension.IkvmModuleExtension;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.module.Module;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.layer.orderEntry.ModuleExtensionWithSdkOrderEntry;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.project.Project;
import consulo.util.io.FileUtil;
import consulo.util.lang.Pair;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.archive.ArchiveVfsUtil;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.virtualFileSystem.util.VirtualFileVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 09.05.14
 */
@ExtensionImpl
public class IkvmStubGenerationCompiler implements SourceProcessingCompiler
{
	public static class Item implements ProcessingItem
	{
		private final File myFile;
		private final Module myAffectedModule;

		public Item(File file, Module affectedModule)
		{
			myFile = file;
			myAffectedModule = affectedModule;
		}

		@NotNull
		@Override
		public File getFile()
		{
			return myFile;
		}

		@Nullable
		@Override
		public ValidityState getValidityState()
		{
			return null;
		}

		public Module getAffectedModule()
		{
			return myAffectedModule;
		}
	}

	@NotNull
	@Override
	public ProcessingItem[] getProcessingItems(CompileContext compileContext)
	{
		Module[] affectedModules = compileContext.getCompileScope().getAffectedModules();
		List<ProcessingItem> items = new ArrayList<ProcessingItem>();
		for(Module affectedModule : affectedModules)
		{
			IkvmModuleExtension extension = affectedModule.getExtension(IkvmModuleExtension.class);
			if(extension == null)
			{
				continue;
			}

			Set<File> files = DotNetCompilerUtil.collectDependencies(affectedModule, DotNetTarget.LIBRARY, true, DotNetCompilerUtil.ACCEPT_ALL);
			for(File file : files)
			{
				items.add(new Item(file, affectedModule));
			}
		}
		return items.toArray(new ProcessingItem[items.size()]);
	}

	@Override
	public ProcessingItem[] process(CompileContext compileContext, ProcessingItem[] processingItems)
	{
		File genDir = new File(ContainerPathManager.get().getSystemPath(), "ikvm-stubs");

		for(ProcessingItem p : processingItems)
		{
			Item item = (Item) p;

			List<DotNetTypeDeclaration> typeDeclarations = collectTypes(compileContext.getProject(), item.getFile());
			for(DotNetTypeDeclaration typeDeclaration : typeDeclarations)
			{
				if(!ReadAction.compute(() -> typeDeclaration.hasModifier(DotNetModifier.PUBLIC)))
				{
					continue;
				}
				String typeName = ApplicationManager.getApplication().runReadAction((Computable<String>) () -> typeDeclaration.getPresentableQName());

				compileContext.getProgressIndicator().setText("Processing: " + typeName);

				Pair<String, byte[]> build = ApplicationManager.getApplication().runReadAction(new Computable<Pair<String, byte[]>>()
				{
					@Override
					public Pair<String, byte[]> compute()
					{
						JavaClassStubBuilder classStubBuilder = StubBuilder.build(typeDeclaration);
						if(classStubBuilder == null)
						{
							return null;
						}
						return Pair.create(classStubBuilder.getQualifiedName(), classStubBuilder.buildToBytecode());
					}
				});

				if(build == null)
				{
					continue;
				}
				String qualifiedName = build.getFirst();

				Module affectedModule = item.getAffectedModule();
				String dirName = affectedModule.getName() + "@" + affectedModule.getModuleDirUrl().hashCode();

				File file = new File(new File(genDir, dirName), qualifiedName.replace(".", "/") + ".class");
				file.getParentFile().mkdirs();

				try
				{
					FileUtil.writeToFile(file, build.getSecond());
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}

			}
		}
		return processingItems;
	}

	private List<DotNetTypeDeclaration> collectTypes(Project project, File file)
	{
		VirtualFile fileByIoFile = LocalFileSystem.getInstance().findFileByIoFile(file);
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
			if(orderEntry instanceof ModuleExtensionWithSdkOrderEntry)
			{
				Sdk sdk = ((ModuleExtensionWithSdkOrderEntry) orderEntry).getSdk();
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

		PsiManager psiManager = PsiManager.getInstance(project);
		List<DotNetTypeDeclaration> list = new ArrayList<DotNetTypeDeclaration>();
		VirtualFileUtil.visitChildrenRecursively(archiveRootForLocalFile, new VirtualFileVisitor()
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
						if(!(psiFile instanceof DotNetMemberOwner))
						{
							return;
						}

						list.addAll(PsiTreeUtil.findChildrenOfType(psiFile, DotNetTypeDeclaration.class));
					}
				});
				return true;
			}
		});
		return list;
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
	public ValidityState createValidityState(DataInput dataInput) throws IOException
	{
		return null;
	}
}
