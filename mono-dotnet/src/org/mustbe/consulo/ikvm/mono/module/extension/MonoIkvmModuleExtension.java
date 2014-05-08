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

package org.mustbe.consulo.ikvm.mono.module.extension;

import org.consulo.java.platform.module.extension.SpecialDirLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import org.mustbe.consulo.ikvm.bundle.IkvmBundleType;
import org.mustbe.consulo.ikvm.compiler.IkvmCompilerOptionsBuilder;
import org.mustbe.consulo.ikvm.module.extension.IkvmModuleExtension;
import org.mustbe.consulo.mono.csharp.module.extension.InnerMonoModuleExtension;
import com.intellij.compiler.impl.ModuleChunk;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.SdkImpl;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.util.ArchiveVfsUtil;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.util.PathsList;

/**
 * @author VISTALL
 * @since 05.05.14
 */
public class MonoIkvmModuleExtension extends InnerMonoModuleExtension<MonoIkvmModuleExtension> implements IkvmModuleExtension<MonoIkvmModuleExtension>
{
	public MonoIkvmModuleExtension(@NotNull String id, @NotNull ModifiableRootModel rootModel)
	{
		super(id, rootModel);
	}

	@Override
	protected Sdk createSdk(VirtualFile virtualFile)
	{
		SdkImpl sdk = new SdkImpl("Mono IKVM.NET", IkvmBundleType.getInstance());
		VirtualFile mainMonoPath = virtualFile.getParent().getParent().getParent();
		sdk.setHomePath(mainMonoPath.getPath());
		sdk.setBundled();
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
					sdkModificator.addRoot(archiveLibraryFile, OrderRootType.CLASSES);
				}
			}
		}

		sdkModificator.commitChanges();
		return sdk;
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
		return LanguageLevel.JDK_1_6;
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
		return SdkTable.getInstance().findBundleSdkByType(JavaSdk.class);
	}

	@NotNull
	@Override
	public String getCompilationClasspath(@NotNull ModuleChunk moduleChunk)
	{
		Sdk sdkForCompilation = getSdkForCompilation();
		PathsList classpath = new PathsList();

		classpath.addVirtualFiles(sdkForCompilation.getRootProvider().getFiles(OrderRootType.CLASSES));

		classpath.addVirtualFiles(VfsUtil.toVirtualFileArray(moduleChunk.getCompilationClasspathFiles(IkvmBundleType.getInstance())));
		return classpath.getPathsString();
	}

	@NotNull
	@Override
	public String getCompilationBootClasspath(@NotNull ModuleChunk moduleChunk)
	{
		return "";
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
}
