package consulo.ikvm.microsoft.module.extension;

import javax.swing.JComponent;

import org.consulo.module.extension.MutableModuleInheritableNamedPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import consulo.ikvm.module.extension.IkvmMutableModuleExtension;
import consulo.ikvm.module.extension.ui.IkvmModuleExtensionPanel;
import org.mustbe.consulo.java.module.extension.SpecialDirLocation;
import org.mustbe.consulo.sdk.SdkUtil;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootLayer;
import com.intellij.openapi.util.Comparing;
import com.intellij.pom.java.LanguageLevel;

/**
 * @author VISTALL
 * @since 07.05.14
 */
public class MicrosoftIkvmMutableModuleExtension extends MicrosoftIkvmModuleExtension implements
		IkvmMutableModuleExtension<MicrosoftIkvmModuleExtension>
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
	@Override
	@RequiredDispatchThread
	public JComponent createConfigurablePanel(@NotNull Runnable updateOnCheck)
	{
		return new IkvmModuleExtensionPanel(this, updateOnCheck, true);
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
}
