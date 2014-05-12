package org.mustbe.consulo.ikvm.microsoft.module.extension;

import javax.swing.JComponent;

import org.consulo.java.platform.module.extension.SpecialDirLocation;
import org.consulo.module.extension.MutableModuleInheritableNamedPointer;
import org.consulo.sdk.SdkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.ikvm.module.extension.IkvmMutableModuleExtension;
import org.mustbe.consulo.ikvm.module.extension.ui.IkvmModuleExtensionPanel;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Comparing;
import com.intellij.pom.java.LanguageLevel;

/**
 * @author VISTALL
 * @since 07.05.14
 */
public class MicrosoftIkvmMutableModuleExtension extends MicrosoftIkvmModuleExtension implements
		IkvmMutableModuleExtension<MicrosoftIkvmModuleExtension>
{
	public MicrosoftIkvmMutableModuleExtension(@NotNull String id, @NotNull ModifiableRootModel rootModel)
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
	public JComponent createConfigurablePanel(@NotNull Runnable updateOnCheck)
	{
		return wrapToNorth(new IkvmModuleExtensionPanel(this, updateOnCheck, true));
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
}
