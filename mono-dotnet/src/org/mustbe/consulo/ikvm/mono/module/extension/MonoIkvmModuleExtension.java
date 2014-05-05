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

import org.consulo.java.platform.module.extension.SpecialDirLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.ikvm.bundle.IkvmBundleType;
import org.mustbe.consulo.ikvm.module.extension.IkvmModuleExtension;
import org.mustbe.consulo.mono.csharp.module.extension.InnerMonoModuleExtension;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.SdkImpl;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;

/**
 * @author VISTALL
 * @since 05.05.14
 */
public class MonoIkvmModuleExtension extends InnerMonoModuleExtension<MonoIkvmModuleExtension> implements IkvmModuleExtension<MonoIkvmModuleExtension>
{
	public MonoIkvmModuleExtension(@NotNull String id, @NotNull ModifiableRootModel rootModel)
	{
		super(id, rootModel);
	}

	@Override
	protected Sdk createSdk(VirtualFile virtualFile)
	{
		SdkImpl sdk = new SdkImpl("Mono IKVM.NET", IkvmBundleType.getInstance());
		VirtualFile virtualFile1 = virtualFile.getParent().getParent().getParent();
		sdk.setHomePath(virtualFile1.getPath());
		sdk.setBundled();
		sdk.setVersionString(IkvmBundleType.getInstance().getVersionString(sdk));
		return sdk;
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
		return LanguageLevel.HIGHEST;
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
		return null;
	}
}
