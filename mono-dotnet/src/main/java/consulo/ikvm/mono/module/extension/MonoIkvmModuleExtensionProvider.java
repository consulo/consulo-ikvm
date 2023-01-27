package consulo.ikvm.mono.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.java.language.impl.icon.JavaPsiImplIconGroup;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 27/01/2023
 */
@ExtensionImpl
public class MonoIkvmModuleExtensionProvider implements ModuleExtensionProvider<MonoIkvmModuleExtension>
{
	@Nonnull
	@Override
	public String getId()
	{
		return "mono-ikvm";
	}

	@Nullable
	@Override
	public String getParentId()
	{
		return "mono-dotnet";
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
	public ModuleExtension<MonoIkvmModuleExtension> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new MonoIkvmModuleExtension(getId(), moduleRootLayer);
	}

	@Nonnull
	@Override
	public MutableModuleExtension<MonoIkvmModuleExtension> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new MonoIkvmMutableModuleExtension(getId(), moduleRootLayer);
	}
}
