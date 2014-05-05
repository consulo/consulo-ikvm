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

import org.consulo.module.extension.MutableModuleExtensionWithSdk;
import org.consulo.module.extension.MutableModuleInheritableNamedPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModifiableRootModel;

/**
 * @author VISTALL
 * @since 05.05.14
 */
public class MonoIkvmMutableModuleExtension extends MonoIkvmModuleExtension implements MutableModuleExtensionWithSdk<MonoIkvmModuleExtension>
{
	public MonoIkvmMutableModuleExtension(@NotNull String id, @NotNull ModifiableRootModel rootModel)
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
		return null;
	}

	@Override
	public void setEnabled(boolean val)
	{
		setEnabledImpl(val);
	}

	@Override
	public boolean isModified(@NotNull MonoIkvmModuleExtension originalExtension)
	{
		return isEnabled() != originalExtension.isEnabled();
	}
}
