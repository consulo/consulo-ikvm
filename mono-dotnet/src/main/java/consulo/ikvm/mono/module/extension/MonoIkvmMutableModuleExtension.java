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

package consulo.ikvm.mono.module.extension;

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

import jakarta.annotation.Nonnull;
import javax.swing.*;
import java.util.List;


/**
 * @author VISTALL
 * @since 05.05.14
 */
public class MonoIkvmMutableModuleExtension extends MonoIkvmModuleExtension implements IkvmMutableModuleExtension<MonoIkvmModuleExtension>, SwingMutableModuleExtension
{
	public MonoIkvmMutableModuleExtension(@NotNull String id, @NotNull ModuleRootLayer rootModel)
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
	@RequiredUIAccess
	public JComponent createConfigurablePanel(Disposable disposable, @NotNull Runnable updateOnCheck)
	{
		return new IkvmModuleExtensionPanel(this, updateOnCheck, false);
	}

	@RequiredUIAccess
	@Nullable
	@Override
	public Component createConfigurationComponent(@Nonnull Disposable disposable, @Nonnull Runnable runnable)
	{
		return null;
	}

	@Override
	public void setBytecodeVersion(@Nullable String s)
	{
	}

	@Override
	public void setCompilerArguments(@Nonnull List<String> list)
	{

	}

	@Override
	public void setEnabled(boolean val)
	{
		setEnabledImpl(val);
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

	@Override
	public boolean isModified(@NotNull MonoIkvmModuleExtension originalExtension)
	{
		return isEnabled() != originalExtension.isEnabled() ||
				!Comparing.equal(mySdkForCompilationPointer, originalExtension.mySdkForCompilationPointer) ||
				!myLanguageLevelPointer.equals(originalExtension.myLanguageLevelPointer);
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
