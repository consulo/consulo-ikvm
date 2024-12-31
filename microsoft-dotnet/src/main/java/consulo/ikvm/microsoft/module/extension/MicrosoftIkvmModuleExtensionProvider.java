package consulo.ikvm.microsoft.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.java.language.impl.icon.JavaPsiImplIconGroup;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 27/01/2023
 */
@ExtensionImpl
public class MicrosoftIkvmModuleExtensionProvider implements ModuleExtensionProvider<MicrosoftIkvmModuleExtension>
{
	@Nonnull
	@Override
	public String getId()
	{
		return "microsoft-ikvm";
	}

	@Nullable
	@Override
	public String getParentId()
	{
		return "microsoft-dotnet";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.localizeTODO("Java (IKVM.NET)");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return JavaPsiImplIconGroup.java();
	}

	@Nonnull
	@Override
	public ModuleExtension<MicrosoftIkvmModuleExtension> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new MicrosoftIkvmModuleExtension(getId(), moduleRootLayer);
	}

	@Nonnull
	@Override
	public MutableModuleExtension<MicrosoftIkvmModuleExtension> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new MicrosoftIkvmMutableModuleExtension(getId(), moduleRootLayer);
	}
}
