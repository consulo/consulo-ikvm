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

package consulo.ikvm.compiler;

import consulo.compiler.CompilerMessageCategory;
import consulo.compiler.ModuleCompilerPathsManager;
import consulo.content.bundle.Sdk;
import consulo.dotnet.DotNetTarget;
import consulo.dotnet.compiler.DotNetCompilerMessage;
import consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import consulo.dotnet.compiler.DotNetMacroUtil;
import consulo.dotnet.impl.compiler.DotNetCompilerUtil;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.ikvm.module.extension.IkvmModuleExtension;
import consulo.language.content.ProductionContentFolderTypeProvider;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.layer.orderEntry.ModuleExtensionWithSdkOrderEntry;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.process.cmd.GeneralCommandLine;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.function.Condition;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 07.05.14
 */
public class IkvmCompilerOptionsBuilder implements DotNetCompilerOptionsBuilder
{
	private static final class IkvmSdkSkipper implements Condition<OrderEntry>
	{
		public static final IkvmSdkSkipper INSTANCE = new IkvmSdkSkipper();

		@Override
		public boolean value(OrderEntry orderEntry)
		{
			return orderEntry instanceof ModuleExtensionWithSdkOrderEntry && ((ModuleExtensionWithSdkOrderEntry) orderEntry).getModuleExtension()
					instanceof IkvmModuleExtension;
		}
	}

	private String myExecutable;

	private List<String> myExtraParameters = new ArrayList<String>();
	private List<String> myArguments = new ArrayList<String>();

	public IkvmCompilerOptionsBuilder(String executable)
	{
		myExecutable = executable;
	}

	public void addExtraParameter(String param)
	{
		myExtraParameters.add(param);
	}

	public void addArgument(String param)
	{
		myArguments.add(param);
	}

	@Nullable
	@Override
	public DotNetCompilerMessage convertToMessage(Module module, String s)
	{
		return new DotNetCompilerMessage(CompilerMessageCategory.INFORMATION, s, null, -1, -1);
	}

	@NotNull
	@Override
	public GeneralCommandLine createCommandLine(@NotNull Module module,
												@NotNull VirtualFile[] virtualFiles,
												@NotNull DotNetModuleExtension extension) throws IOException
	{
		IkvmModuleExtension ikvmModuleExtension = ModuleUtilCore.getExtension(module, IkvmModuleExtension.class);
		assert ikvmModuleExtension != null;
		Sdk sdk = ikvmModuleExtension.getSdk();
		assert sdk != null;

		GeneralCommandLine generalCommandLine = new GeneralCommandLine();
		generalCommandLine.setExePath(sdk.getHomePath() + "/" + myExecutable);
		for(String extraParameter : myExtraParameters)
		{
			generalCommandLine.addParameter(extraParameter);
		}

		String target = null;
		switch(extension.getTarget())
		{
			case EXECUTABLE:
				target = "exe";
				break;
			case LIBRARY:
				target = "library";
				break;
		}

		addArgument("-nostdlib");

		addArgument("-target:" + target);
		String outputFile = DotNetMacroUtil.expandOutputFile(extension);
		addArgument("-out:" + outputFile);

		Set<File> dependFiles = DotNetCompilerUtil.collectDependencies(module, DotNetTarget.LIBRARY, true, IkvmSdkSkipper.INSTANCE);

		for(File file : dependFiles)
		{
			addArgument("-reference:" + file.getAbsolutePath());
		}

		if(extension.isAllowDebugInfo())
		{
			addArgument("-debug");
		}

		String mainType = extension.getMainType();
		if(!StringUtil.isEmpty(mainType))
		{
			addArgument("-main:" + mainType);
		}

		ModuleCompilerPathsManager pathsManager = ModuleCompilerPathsManager.getInstance(module);

		String path = pathsManager.getCompilerOutput(ProductionContentFolderTypeProvider.getInstance()).getPath();
		generalCommandLine.setWorkDirectory(path);
		addArgument("-recurse:" + path + "/");

		StringBuilder rspText = new StringBuilder();
		File tempFile = FileUtil.createTempFile("consulo-ikvm-rsp", ".rsp");
		for(String argument : myArguments)
		{
			rspText.append(argument);
			rspText.append("\n");
		}
		FileUtil.writeToFile(tempFile, rspText.toString());

		//LOGGER.warn("Compiler def file: " + tempFile);
		//LOGGER.warn(FileUtil.loadFile(tempFile));

		FileUtil.createParentDirs(new File(outputFile));

		generalCommandLine.addParameter("@" + tempFile.getAbsolutePath());
		generalCommandLine.setRedirectErrorStream(true);
		return generalCommandLine;
	}
}
