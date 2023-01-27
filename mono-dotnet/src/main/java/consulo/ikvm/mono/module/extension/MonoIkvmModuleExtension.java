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

package consulo.ikvm.mono.module.extension;

import com.intellij.java.language.LanguageLevel;
import com.intellij.java.language.impl.JavaFileType;
import consulo.annotation.access.RequiredReadAction;
import consulo.compiler.CompileContext;
import consulo.compiler.ModuleChunk;
import consulo.component.util.pointer.NamedPointer;
import consulo.container.boot.ContainerPathManager;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.bundle.*;
import consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import consulo.ikvm.bundle.IkvmBundleType;
import consulo.ikvm.compiler.IkvmCompilerOptionsBuilder;
import consulo.ikvm.module.extension.IkvmModuleExtension;
import consulo.ikvm.module.extension.IkvmModuleExtensionUtil;
import consulo.java.impl.module.extension.LanguageLevelModuleInheritableNamedPointerImpl;
import consulo.java.language.module.extension.SpecialDirLocation;
import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiElement;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleInheritableNamedPointer;
import consulo.mono.dotnet.module.extension.InnerMonoModuleExtension;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.OrderedSet;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.archive.ArchiveVfsUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 05.05.14
 */
public class MonoIkvmModuleExtension extends InnerMonoModuleExtension<MonoIkvmModuleExtension> implements IkvmModuleExtension<MonoIkvmModuleExtension>
{
	protected NamedPointer<Sdk> mySdkForCompilationPointer;
	protected final LanguageLevelModuleInheritableNamedPointerImpl myLanguageLevelPointer;

	public MonoIkvmModuleExtension(@NotNull String id, @NotNull ModuleRootLayer moduleRootLayer)
	{
		super(id, moduleRootLayer);
		myLanguageLevelPointer = new LanguageLevelModuleInheritableNamedPointerImpl(moduleRootLayer, id);
	}

	@Override
	protected Sdk createSdk(VirtualFile virtualFile)
	{
		Sdk sdk = SdkTable.getInstance().createSdk("Mono IKVM.NET", IkvmBundleType.getInstance());
		VirtualFile mainMonoPath = virtualFile.getParent().getParent().getParent();

		SdkModificator sdkModificator = sdk.getSdkModificator();

		sdkModificator.setHomePath(mainMonoPath.getPath());
		sdkModificator.setVersionString(IkvmBundleType.getInstance().getVersionString(sdk));

		for(String library : IkvmBundleType.ourLibraries)
		{
			VirtualFile libraryFile = mainMonoPath.findFileByRelativePath("lib/mono/ikvm/" + library);
			if(libraryFile != null)
			{
				VirtualFile archiveLibraryFile = ArchiveVfsUtil.getArchiveRootForLocalFile(libraryFile);
				if(archiveLibraryFile != null)
				{
					sdkModificator.addRoot(archiveLibraryFile, BinariesOrderRootType.getInstance());
				}
			}
		}

		sdkModificator.commitChanges();
		return sdk;
	}

	@Override
	public void commit(@NotNull MonoIkvmModuleExtension mutableModuleExtension)
	{
		super.commit(mutableModuleExtension);
		mySdkForCompilationPointer = mutableModuleExtension.mySdkForCompilationPointer;
		myLanguageLevelPointer.set(mutableModuleExtension.getInheritableLanguageLevel());
	}

	@NotNull
	@Override
	public Class<? extends SdkType> getSdkTypeClass()
	{
		return IkvmBundleType.class;
	}

	@NotNull
	@Override
	public LanguageLevel getLanguageLevel()
	{
		return myLanguageLevelPointer.get();
	}

	@NotNull
	@Override
	public SpecialDirLocation getSpecialDirLocation()
	{
		return SpecialDirLocation.SOURCE_DIR;
	}

	@Nullable
	@Override
	public Sdk getSdkForCompilation()
	{
		return mySdkForCompilationPointer == null ? null : mySdkForCompilationPointer.get();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getAssemblyTitle()
	{
		return null;
	}

	@Nullable
	@Override
	public String getJavaSdkName()
	{
		return mySdkForCompilationPointer == null ? null : mySdkForCompilationPointer.getName();
	}

	@NotNull
	public ModuleInheritableNamedPointer<LanguageLevel> getInheritableLanguageLevel()
	{
		return myLanguageLevelPointer;
	}

	@NotNull
	@Override
	public Set<VirtualFile> getCompilationClasspath(@NotNull CompileContext compileContext, @NotNull ModuleChunk moduleChunk)
	{
		Sdk sdkForCompilation = getSdkForCompilation();
		Set<VirtualFile> files = new OrderedSet<>();

		ContainerUtil.addAll(files, sdkForCompilation.getRootProvider().getFiles(BinariesOrderRootType.getInstance()));

		VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(ContainerPathManager.get().getSystemPath() + "/ikvm-stubs/" + getModule().getName() + "@" + getModule().getModuleDirUrl().hashCode());
		if(fileByPath != null)
		{
			files.add(fileByPath);
		}

		files.addAll(moduleChunk.getCompilationClasspathFiles(IkvmBundleType.getInstance()));
		return files;
	}

	@NotNull
	@Override
	public Set<VirtualFile> getCompilationBootClasspath(@NotNull CompileContext compileContext, @NotNull ModuleChunk moduleChunk)
	{
		return Collections.emptySet();
	}

	@Nullable
	@Override
	public String getBytecodeVersion()
	{
		return null;
	}

	@Nonnull
	@Override
	public List<String> getCompilerArguments()
	{
		return List.of();
	}

	@RequiredReadAction
	@NotNull
	@Override
	public PsiElement[] getEntryPointElements()
	{
		return IkvmModuleExtensionUtil.buildEntryPoints(getModule());
	}

	@NotNull
	@Override
	public LanguageFileType getFileType()
	{
		return JavaFileType.INSTANCE;
	}

	@NotNull
	@Override
	public DotNetCompilerOptionsBuilder createCompilerOptionsBuilder()
	{
		IkvmCompilerOptionsBuilder ikvmCompilerOptionsBuilder = new IkvmCompilerOptionsBuilder("bin/mono");
		ikvmCompilerOptionsBuilder.addExtraParameter(getSdk().getHomePath() + "/lib/ikvm/ikvmc.exe");
		return ikvmCompilerOptionsBuilder;
	}

	@RequiredReadAction
	@Override
	protected void loadStateImpl(@NotNull Element element)
	{
		super.loadStateImpl(element);
		myLanguageLevelPointer.fromXml(element);
		String sdkForCompilation = element.getAttributeValue("sdk-for-compilation");
		if(sdkForCompilation != null)
		{
			mySdkForCompilationPointer = SdkUtil.createPointer(sdkForCompilation);
		}
	}

	@Override
	protected void getStateImpl(@NotNull Element element)
	{
		super.getStateImpl(element);
		myLanguageLevelPointer.toXml(element);
		if(mySdkForCompilationPointer != null)
		{
			element.setAttribute("sdk-for-compilation", mySdkForCompilationPointer.getName());
		}
	}
}
