package consulo.ikvm.microsoft.module.extension;

import com.intellij.java.language.LanguageLevel;
import com.intellij.java.language.impl.JavaFileType;
import consulo.compiler.CompileContext;
import consulo.compiler.ModuleChunk;
import consulo.component.util.pointer.NamedPointer;
import consulo.container.boot.ContainerPathManager;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkType;
import consulo.content.bundle.SdkUtil;
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
import consulo.module.content.layer.extension.ModuleExtensionWithSdkBase;
import consulo.module.extension.ModuleExtensionWithSdk;
import consulo.module.extension.ModuleInheritableNamedPointer;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.OrderedSet;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 07.05.14
 */
public class MicrosoftIkvmModuleExtension extends ModuleExtensionWithSdkBase<MicrosoftIkvmModuleExtension> implements ModuleExtensionWithSdk<MicrosoftIkvmModuleExtension>, IkvmModuleExtension<MicrosoftIkvmModuleExtension>
{
	protected NamedPointer<Sdk> mySdkForCompilationPointer;
	protected final LanguageLevelModuleInheritableNamedPointerImpl myLanguageLevelPointer;

	public MicrosoftIkvmModuleExtension(@NotNull final String id, @NotNull ModuleRootLayer layer)
	{
		super(id, layer);
		myLanguageLevelPointer = new LanguageLevelModuleInheritableNamedPointerImpl(layer, id);
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
	public Set<VirtualFile> getCompilationClasspath(@NotNull CompileContext compileContext, @NotNull ModuleChunk moduleChunk)
	{
		Sdk sdkForCompilation = getSdkForCompilation();
		if(sdkForCompilation == null)
		{
			return Collections.emptySet();
		}

		Set<VirtualFile> files = new OrderedSet<>();

		ContainerUtil.addAll(files, sdkForCompilation.getRootProvider().getFiles(BinariesOrderRootType.getInstance()));

		files.addAll(moduleChunk.getCompilationClasspathFiles(IkvmBundleType.getInstance()));

		VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(ContainerPathManager.get().getSystemPath() + "/ikvm-stubs/" + getModule().getName() + "@" + getModule().getModuleDirUrl().hashCode());
		if(fileByPath != null)
		{
			files.add(fileByPath);
		}
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

	@NotNull
	@Override
	public PsiElement[] getEntryPointElements()
	{
		return IkvmModuleExtensionUtil.buildEntryPoints(getModule());
	}

	@Nullable
	@Override
	public String getAssemblyTitle()
	{
		return null;
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
