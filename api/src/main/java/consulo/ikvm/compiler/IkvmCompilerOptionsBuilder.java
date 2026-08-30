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
import consulo.dotnet.compiler.DotNetCompilerUtil;
import consulo.dotnet.compiler.DotNetMacroUtil;
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
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author VISTALL
 * @since 07.05.14
 */
public class IkvmCompilerOptionsBuilder implements DotNetCompilerOptionsBuilder {
    private static final Predicate<OrderEntry> IKVM_SDK_SKIPPER =
        orderEntry -> orderEntry instanceof ModuleExtensionWithSdkOrderEntry sdkOrderEntry
            && sdkOrderEntry.getModuleExtension() instanceof IkvmModuleExtension;

    private String myExecutable;

    private List<String> myExtraParameters = new ArrayList<>();
    private List<String> myArguments = new ArrayList<>();

    public IkvmCompilerOptionsBuilder(String executable) {
        myExecutable = executable;
    }

    public void addExtraParameter(String param) {
        myExtraParameters.add(param);
    }

    public void addArgument(String param) {
        myArguments.add(param);
    }

    @Override
    public @Nullable DotNetCompilerMessage convertToMessage(Module module, String line) {
        return new DotNetCompilerMessage(CompilerMessageCategory.INFORMATION, line, null, -1, -1);
    }

    @Override
    public GeneralCommandLine createCommandLine(Module module, Collection<Path> results, DotNetModuleExtension<?> extension) throws IOException {
        IkvmModuleExtension ikvmModuleExtension = ModuleUtilCore.getExtension(module, IkvmModuleExtension.class);
        assert ikvmModuleExtension != null;
        Sdk sdk = ikvmModuleExtension.getSdk();
        assert sdk != null;

        GeneralCommandLine generalCommandLine = new GeneralCommandLine();
        generalCommandLine.setExePath(sdk.getHomePath() + "/" + myExecutable);
        for (String extraParameter : myExtraParameters) {
            generalCommandLine.addParameter(extraParameter);
        }

        String target = null;
        switch (extension.getTarget()) {
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

        Set<File> dependFiles = DotNetCompilerUtil.collectDependencies(module, DotNetTarget.LIBRARY, true, IKVM_SDK_SKIPPER);

        for (File file : dependFiles) {
            addArgument("-reference:" + file.getAbsolutePath());
        }

        if (extension.isAllowDebugInfo()) {
            addArgument("-debug");
        }

        String mainType = extension.getMainType();
        if (!StringUtil.isEmpty(mainType)) {
            addArgument("-main:" + mainType);
        }

        ModuleCompilerPathsManager pathsManager = ModuleCompilerPathsManager.getInstance(module);

        Path outputPath = pathsManager.getCompilerOutputPath(ProductionContentFolderTypeProvider.getInstance());
        assert outputPath != null;
        String path = FileUtil.toSystemIndependentName(outputPath.toString());
        generalCommandLine.setWorkDirectory(path);
        addArgument("-recurse:" + path + "/");

        StringBuilder rspText = new StringBuilder();
        File tempFile = FileUtil.createTempFile("consulo-ikvm-rsp", ".rsp");
        for (String argument : myArguments) {
            rspText.append(argument);
            rspText.append("\n");
        }
        FileUtil.writeToFile(tempFile, rspText.toString());

        FileUtil.createParentDirs(new File(outputFile));

        generalCommandLine.addParameter("@" + tempFile.getAbsolutePath());
        generalCommandLine.setRedirectErrorStream(true);
        return generalCommandLine;
    }
}
