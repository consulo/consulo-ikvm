package org.mustbe.consulo.ikvm.microsoft.module.extension;

import org.consulo.java.platform.module.extension.LanguageLevelModuleInheritableNamedPointerImpl;
import org.consulo.java.platform.module.extension.SpecialDirLocation;
import org.consulo.module.extension.ModuleExtensionWithSdk;
import org.consulo.module.extension.ModuleInheritableNamedPointer;
import org.consulo.module.extension.impl.ModuleExtensionWithSdkImpl;
import org.consulo.sdk.SdkUtil;
import org.consulo.util.pointers.NamedPointer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import org.mustbe.consulo.ikvm.bundle.IkvmBundleType;
import org.mustbe.consulo.ikvm.compiler.IkvmCompilerOptionsBuilder;
import org.mustbe.consulo.ikvm.module.extension.IkvmModuleExtension;
import com.intellij.compiler.impl.ModuleChunk;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModuleRootLayer;
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
	protected NamedPointer<Sdk> mySdkForCompilationPointer;
	protected final LanguageLevelModuleInheritableNamedPointerImpl myLanguageLevelPointer;

	public MicrosoftIkvmModuleExtension(@NotNull final String id, @NotNull ModuleRootLayer rootModel)
	{
		super(id, rootModel);
		myLanguageLevelPointer = new LanguageLevelModuleInheritableNamedPointerImpl(getProject(), id);
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

	@Nullable
	@Override
	public String getJavaSdkName()
	{
		return mySdkForCompilationPointer == null ? null : mySdkForCompilationPointer.getName();
	}

	@Override
	public void commit(@NotNull MicrosoftIkvmModuleExtension mutableModuleExtension)
	{
		super.commit(mutableModuleExtension);
		mySdkForCompilationPointer = mutableModuleExtension.mySdkForCompilationPointer;
		myLanguageLevelPointer.set(mutableModuleExtension.getInheritableLanguageLevel());
	}

	@NotNull
	public ModuleInheritableNamedPointer<LanguageLevel> getInheritableLanguageLevel()
	{
		return myLanguageLevelPointer;
	}

	@NotNull
	@Override
	public String getCompilationClasspath(@NotNull CompileContext compileContext, @NotNull ModuleChunk moduleChunk)
	{
		Sdk sdkForCompilation = getSdkForCompilation();
		if(sdkForCompilation == null)
		{
			return "";
		}
		PathsList classpath = new PathsList();

		classpath.addVirtualFiles(sdkForCompilation.getRootProvider().getFiles(OrderRootType.BINARIES));

		classpath.addVirtualFiles(VfsUtil.toVirtualFileArray(moduleChunk.getCompilationClasspathFiles(IkvmBundleType.getInstance())));

		classpath.add(PathManager.getSystemPath() + "/ikvm-stubs/" + getModule().getName() + "@" + getModule().getModuleDirUrl().hashCode());
		return classpath.getPathsString();
	}

	@NotNull
	@Override
	public String getCompilationBootClasspath(@NotNull CompileContext compileContext, @NotNull ModuleChunk moduleChunk)
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
