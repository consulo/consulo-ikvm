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

package consulo.ikvm.microsoft.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.compiler.DotNetDependencyCopierExtension;
import consulo.ikvm.microsoft.module.extension.MicrosoftIkvmModuleExtension;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.types.BinariesOrderRootType;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 25.09.14
 */
public class IkvmDependencyCopierExtension implements DotNetDependencyCopierExtension
{
	@NotNull
	@Override
	public List<File> collectDependencies(@NotNull Module module)
	{
		Sdk ikvmSdk = ModuleUtilCore.getSdk(module, MicrosoftIkvmModuleExtension.class);
		if(ikvmSdk != null)
		{
			VirtualFile[] files = ikvmSdk.getRootProvider().getFiles(BinariesOrderRootType.getInstance());
			List<File> fileList = new ArrayList<File>(files.length);
			for(VirtualFile file : files)
			{
				fileList.add(VfsUtil.virtualToIoFile(file));
			}
			return fileList;
		}
		return Collections.emptyList();
	}
}
