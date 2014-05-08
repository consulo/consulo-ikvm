package org.mustbe.consulo.ikvm.microsoft.module.extension;

import org.consulo.java.platform.module.extension.SpecialDirLocation;
import org.consulo.module.extension.ModuleExtensionWithSdk;
import org.consulo.module.extension.impl.ModuleExtensionWithSdkImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import org.mustbe.consulo.ikvm.bundle.IkvmBundleType;
import org.mustbe.consulo.ikvm.compiler.IkvmCompilerOptionsBuilder;
import org.mustbe.consulo.ikvm.module.extension.IkvmModuleExtension;
import com.intellij.compiler.impl.ModuleChunk;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.util.PathsList;

/**
 * @author VISTALL
 * @since 07.05.14
 */
public class MicrosoftIkvmModuleExtension extends ModuleExtensionWithSdkImpl<MicrosoftIkvmModuleExtension> implements
		ModuleExtensionWithSdk<MicrosoftIkvmModuleExtension>, IkvmModuleExtension<MicrosoftIkvmModuleExtension>

{
	public MicrosoftIkvmModuleExtension(@NotNull String id, @NotNull ModifiableRootModel rootModel)
	{
		super(id, rootModel);
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
		IkvmCompilerOptionsBuilder builder = new IkvmCompilerOptionsBuilder("bin/ikvmc.exe");
		builder.addArgument("-nologo");
		return builder;
	}
}
