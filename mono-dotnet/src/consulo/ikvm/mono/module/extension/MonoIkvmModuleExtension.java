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

import java.util.Collections;
import java.util.Set;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.compiler.impl.ModuleChunk;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.SdkImpl;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.OrderedSet;
import consulo.annotations.RequiredReadAction;
import consulo.bundle.SdkUtil;
import consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import consulo.ikvm.IkvmModuleExtension;
import consulo.ikvm.bundle.IkvmBundleType;
import consulo.ikvm.compiler.IkvmCompilerOptionsBuilder;
import consulo.ikvm.module.extension.IkvmModuleExtensionUtil;
import consulo.java.module.extension.LanguageLevelModuleInheritableNamedPointerImpl;
import consulo.java.module.extension.SpecialDirLocation;
import consulo.module.extension.ModuleInheritableNamedPointer;
import consulo.mono.dotnet.module.extension.InnerMonoModuleExtension;
import consulo.roots.ModuleRootLayer;
import consulo.roots.types.BinariesOrderRootType;
import consulo.util.pointers.NamedPointer;
import consulo.vfs.util.ArchiveVfsUtil;

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
		SdkImpl sdk = new SdkImpl("Mono IKVM.NET", IkvmBundleType.getInstance());
		VirtualFile mainMonoPath = virtualFile.getParent().getParent().getParent();
		sdk.setHomePath(mainMonoPath.getPath());
		sdk.setPredefined(true);
		sdk.setVersionString(IkvmBundleType.getInstance().getVersionString(sdk));

		SdkModificator sdkModificator = sdk.getSdkModificator();
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

		VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(PathManager.getSystemPath() + "/ikvm-stubs/" + getModule().getName() + "@" + getModule().getModuleDirUrl().hashCode());
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
