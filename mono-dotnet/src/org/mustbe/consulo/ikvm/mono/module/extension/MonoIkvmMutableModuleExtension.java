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

package org.mustbe.consulo.ikvm.mono.module.extension;

import javax.swing.JComponent;

import org.consulo.module.extension.MutableModuleInheritableNamedPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.ikvm.module.extension.IkvmMutableModuleExtension;
import org.mustbe.consulo.ikvm.module.extension.ui.IkvmModuleExtensionPanel;
import org.mustbe.consulo.java.module.extension.SpecialDirLocation;
import org.mustbe.consulo.sdk.SdkUtil;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootLayer;
import com.intellij.openapi.util.Comparing;
import com.intellij.pom.java.LanguageLevel;

/**
 * @author VISTALL
 * @since 05.05.14
 */
public class MonoIkvmMutableModuleExtension extends MonoIkvmModuleExtension implements IkvmMutableModuleExtension<MonoIkvmModuleExtension>
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
	@RequiredDispatchThread
	public JComponent createConfigurablePanel(@NotNull Runnable updateOnCheck)
	{
		return new IkvmModuleExtensionPanel(this, updateOnCheck, false);
	}

	@Override
	public void setBytecodeVersion(@Nullable String s)
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
