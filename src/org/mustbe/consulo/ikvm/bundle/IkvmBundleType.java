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

package org.mustbe.consulo.ikvm.bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.java.JavaIcons;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.util.ExecUtil;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.util.ArchiveVfsUtil;

/**
 * @author VISTALL
 * @since 05.05.14
 */
public class IkvmBundleType extends SdkType
{
	public IkvmBundleType()
	{
		super("IKVM");
	}

	@Nullable
	@Override
	public Icon getIcon()
	{
		return JavaIcons.Java;
	}

	@Nullable
	@Override
	public String suggestHomePath()
	{
		return null;
	}

	@Override
	public boolean isValidSdkHome(String path)
	{
		return new File(path, "bin/ikvm.exe").exists();
	}

	@Override
	public boolean isRootTypeApplicable(OrderRootType type)
	{
		return JavaSdk.getInstance().isRootTypeApplicable(type);
	}

	@Override
	public void setupSdkPaths(Sdk sdk)
	{
		SdkModificator sdkModificator = sdk.getSdkModificator();

		VirtualFile ikvmApi = sdk.getHomeDirectory().findFileByRelativePath("lib/ikvm-api.jar");
		if(ikvmApi != null)
		{
			VirtualFile ikvmJar = ArchiveVfsUtil.getArchiveRootForLocalFile(ikvmApi);
			if(ikvmJar != null)
			{
				sdkModificator.addRoot(ikvmJar, OrderRootType.CLASSES);
			}
		}
		sdkModificator.commitChanges();
	}

	@Nullable
	@Override
	public String getVersionString(String sdkHome)
	{
		List<String> args = new ArrayList<String>(2);
		args.add(sdkHome + "/bin/ikvm.exe");
		args.add("-version");
		try
		{
			ProcessOutput processOutput = ExecUtil.execAndGetOutput(args, sdkHome);
			for(String s : processOutput.getStdoutLines())
			{
				if(s.startsWith("ikvm"))
				{
					return s.substring(5, s.length()).trim();
				}
			}
		}
		catch(ExecutionException e)
		{
			return null;
		}
		return null;
	}

	@Override
	public String suggestSdkName(String currentSdkName, String sdkHome)
	{
		return "ikvm";
	}

	@NotNull
	@Override
	public String getPresentableName()
	{
		return "IKVM.NET";
	}
}
