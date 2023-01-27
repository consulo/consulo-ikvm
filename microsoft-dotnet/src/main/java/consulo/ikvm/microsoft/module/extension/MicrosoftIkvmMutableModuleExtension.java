package consulo.ikvm.microsoft.module.extension;

import com.intellij.java.language.LanguageLevel;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkUtil;
import consulo.disposer.Disposable;
import consulo.ikvm.module.extension.IkvmMutableModuleExtension;
import consulo.ikvm.module.extension.ui.IkvmModuleExtensionPanel;
import consulo.java.language.module.extension.SpecialDirLocation;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.module.extension.swing.SwingMutableModuleExtension;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.Comparing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.List;

/**
 * @author VISTALL
 * @since 07.05.14
 */
public class MicrosoftIkvmMutableModuleExtension extends MicrosoftIkvmModuleExtension implements
		IkvmMutableModuleExtension<MicrosoftIkvmModuleExtension>, SwingMutableModuleExtension
{
	public MicrosoftIkvmMutableModuleExtension(@NotNull String id, @NotNull ModuleRootLayer rootModel)
	{
		super(id, rootModel);
	}

	@NotNull
	@Override
	public MutableModuleInheritableNamedPointer<Sdk> getInheritableSdk()
	{
		return (MutableModuleInheritableNamedPointer<Sdk>) super.getInheritableSdk();
	}

	@Nullable
	@RequiredUIAccess
	public JComponent createConfigurablePanel(Disposable disposable, @NotNull Runnable updateOnCheck)
	{
		return new IkvmModuleExtensionPanel(this, updateOnCheck, true);
	}

	@RequiredUIAccess
	@javax.annotation.Nullable
	@Override
	public Component createConfigurationComponent(@Nonnull Disposable disposable, @Nonnull Runnable runnable)
	{
		return null;
	}

	@Override
	public void setEnabled(boolean val)
	{
		myIsEnabled = val;
	}

	@Override
	public boolean isModified(@NotNull MicrosoftIkvmModuleExtension originalExtension)
	{
		return isModifiedImpl(originalExtension) ||
				!Comparing.equal(mySdkForCompilationPointer, originalExtension.mySdkForCompilationPointer) ||
				!myLanguageLevelPointer.equals(originalExtension.myLanguageLevelPointer);
	}

	@Override
	public void setSdkForCompilation(@Nullable String sdkForCompilation)
	{
		mySdkForCompilationPointer = sdkForCompilation == null ? null : SdkUtil.createPointer(sdkForCompilation);
	}

	@Override
	public void setSdkForCompilation(@Nullable Sdk sdkForCompilation)
	{
		mySdkForCompilationPointer = sdkForCompilation == null ? null : SdkUtil.createPointer(sdkForCompilation);
	}

	@NotNull
	@Override
	public MutableModuleInheritableNamedPointer<LanguageLevel> getInheritableLanguageLevel()
	{
		return myLanguageLevelPointer;
	}

	@Override
	public void setSpecialDirLocation(@NotNull SpecialDirLocation specialDirLocation)
	{

	}

	@Override
	public void setBytecodeVersion(@Nullable String s)
	{

	}

	@Override
	public void setCompilerArguments(@Nonnull List<String> list)
	{
		
	}
}
