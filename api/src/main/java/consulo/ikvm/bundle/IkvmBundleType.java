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

package consulo.ikvm.bundle;

import com.intellij.java.language.projectRoots.JavaSdkType;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.SystemInfo;
import consulo.content.OrderRootType;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkModificator;
import consulo.java.language.impl.JavaIcons;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.util.CapturingProcessUtil;
import consulo.process.util.ProcessOutput;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.archive.ArchiveVfsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author VISTALL
 * @since 05.05.14
 */
@ExtensionImpl
public class IkvmBundleType extends JavaSdkType
{
	public static String[] ourLibraries = new String[]{
			"IKVM.OpenJDK.Core.dll",
			"IKVM.OpenJDK.Util.dll",
			"IKVM.OpenJDK.Text.dll",
			"IKVM.Runtime.dll",
			"IKVM.Java.dll"
	};

	@NotNull
	public static IkvmBundleType getInstance()
	{
		return EP_NAME.findExtensionOrFail(IkvmBundleType.class);
	}

	public static String getExecutable(String sdkHome)
	{
		if(SystemInfo.isWindows)
		{
			return sdkHome + "/bin/java.exe";
		}
		else
		{
			return sdkHome + "/bin/java";
		}
	}

	public IkvmBundleType()
	{
		super("IKVM");
	}

	@Nullable
	@Override
	public Image getIcon()
	{
		return JavaIcons.Java;
	}

	@Override
	public boolean isValidSdkHome(String path)
	{
		return new File(getExecutable(path)).exists();
	}

	@Override
	public boolean isRootTypeApplicable(OrderRootType type)
	{
		return type == BinariesOrderRootType.getInstance();
	}

	@Override
	public void setupSdkPaths(Sdk sdk)
	{
		SdkModificator sdkModificator = sdk.getSdkModificator();

		VirtualFile homeDirectory = sdk.getHomeDirectory();
		assert homeDirectory != null;
		for(String library : ourLibraries)
		{
			VirtualFile ikvmApi = homeDirectory.findFileByRelativePath("bin/" + library);
			if(ikvmApi != null)
			{
				VirtualFile ikvmJar = ArchiveVfsUtil.getArchiveRootForLocalFile(ikvmApi);
				if(ikvmJar != null)
				{
					sdkModificator.addRoot(ikvmJar, BinariesOrderRootType.getInstance());
				}
			}
		}

		sdkModificator.commitChanges();
	}

	@Nullable
	@Override
	public String getVersionString(String sdkHome)
	{
		GeneralCommandLine commandLine = new GeneralCommandLine();
		commandLine.setExePath(getExecutable(sdkHome));
		commandLine.addParameter("-version");

		try
		{
			ProcessOutput output = CapturingProcessUtil.execAndGetOutput(commandLine);

			for(String line : output.getStdoutLines())
			{
				System.out.println(line);
			}
			System.out.println();
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
		return getPresentableName() + " " + getVersionString(sdkHome);
	}

	@NotNull
	@Override
	public String getPresentableName()
	{
		return "IKVM.NET";
	}

	@Override
	public String getBinPath(Sdk sdk)
	{
		return sdk.getHomePath() + "/bin";
	}

	@Override
	public String getToolsPath(Sdk sdk)
	{
		return null;
	}

	@Override
	public void setupCommandLine(@NotNull GeneralCommandLine commandLine, @NotNull Sdk sdk)
	{
		if(new File(sdk.getHomePath(), "bin/java.exe").exists())
		{
			commandLine.setExePath(sdk.getHomePath() + "/bin/ikvm.exe");
		}
	}
}
