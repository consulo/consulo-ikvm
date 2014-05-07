package org.mustbe.consulo.ikvm.microsoft.module.extension;

import javax.swing.JComponent;

import org.consulo.java.module.extension.JavaMutableModuleExtension;
import org.consulo.java.platform.module.extension.SpecialDirLocation;
import org.consulo.module.extension.MutableModuleExtensionWithSdk;
import org.consulo.module.extension.MutableModuleInheritableNamedPointer;
import org.consulo.module.extension.ui.ModuleExtensionWithSdkPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.pom.java.LanguageLevel;

/**
 * @author VISTALL
 * @since 07.05.14
 */
public class MicrosoftIkvmMutableModuleExtension extends MicrosoftIkvmModuleExtension implements
		MutableModuleExtensionWithSdk<MicrosoftIkvmModuleExtension>, JavaMutableModuleExtension<MicrosoftIkvmModuleExtension>
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
		return wrapToNorth(new ModuleExtensionWithSdkPanel(this, updateOnCheck));
	}

	@Override
	public void setEnabled(boolean val)
	{
		myIsEnabled = val;
	}

	@Override
	public boolean isModified(@NotNull MicrosoftIkvmModuleExtension originalExtension)
	{
		return isModifiedImpl(originalExtension);
	}

	@NotNull
	@Override
	public MutableModuleInheritableNamedPointer<LanguageLevel> getInheritableLanguageLevel()
	{
		return null;
	}

	@Override
	public void setSpecialDirLocation(@NotNull SpecialDirLocation specialDirLocation)
	{

	}
}
