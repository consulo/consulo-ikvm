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
import consulo.application.Application;
import consulo.application.util.SystemInfo;
import consulo.content.OrderRootType;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkModificator;
import consulo.content.bundle.SdkType;
import consulo.java.language.impl.JavaIcons;
import consulo.localize.LocalizeValue;
import consulo.platform.Platform;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.util.CapturingProcessUtil;
import consulo.process.util.ProcessOutput;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.archive.ArchiveVfsUtil;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 05.05.14
 */
@ExtensionImpl
public class IkvmBundleType extends JavaSdkType {
    public static String[] ourLibraries = new String[]{
        "IKVM.OpenJDK.Core.dll",
        "IKVM.OpenJDK.Util.dll",
        "IKVM.OpenJDK.Text.dll",
        "IKVM.Runtime.dll",
        "IKVM.Java.dll"
    };

    @Nonnull
    public static IkvmBundleType getInstance() {
        return Application.get().getExtensionPoint(SdkType.class).findExtensionOrFail(IkvmBundleType.class);
    }

    public static String getExecutable(String sdkHome) {
        if (SystemInfo.isWindows) {
            return sdkHome + "/bin/java.exe";
        }
        else {
            return sdkHome + "/bin/java";
        }
    }

    private static Path getExecutable(Platform platform, Path sdkHome) {
        return sdkHome.resolve("bin").resolve(platform.os().isWindows() ? "java.exe" : "java");
    }

    public IkvmBundleType() {
        super("IKVM", LocalizeValue.localizeTODO("IKVM.NET"), JavaIcons.Java);
    }

    @Override
    public void collectHomePaths(Platform platform, Consumer<Path> pathConsumer) {
    }

    @Override
    public boolean isValidSdkHome(Platform platform, Path path) {
        return Files.exists(getExecutable(platform, path));
    }

    @Override
    public boolean isRootTypeApplicable(String type) {
        return BinariesOrderRootType.ID.equals(type);
    }

    @Override
    public void setupSdkPaths(Sdk sdk) {
        SdkModificator sdkModificator = sdk.getSdkModificator();

        VirtualFile homeDirectory = sdk.getHomeDirectory();
        assert homeDirectory != null;
        for (String library : ourLibraries) {
            VirtualFile ikvmApi = homeDirectory.findFileByRelativePath("bin/" + library);
            if (ikvmApi != null) {
                VirtualFile ikvmJar = ArchiveVfsUtil.getArchiveRootForLocalFile(ikvmApi);
                if (ikvmJar != null) {
                    sdkModificator.addRoot(ikvmJar, BinariesOrderRootType.ID);
                }
            }
        }

        sdkModificator.commitChanges();
    }

    @Nullable
    @Override
    public String getVersionString(Platform platform, Path sdkHome) {
        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.withPlatform(platform);
        commandLine.setExePath(getExecutable(platform, sdkHome).toString());
        commandLine.addParameter("-version");

        try {
            ProcessOutput output = CapturingProcessUtil.execAndGetOutput(commandLine);

            for (String line : output.getStdoutLines()) {
                String prefix = "OpenJDK version: ";
                if (line.startsWith(prefix)) {
                    String verWithJdk = line.substring(prefix.length()).trim();
                    String nextPrefix = "OpenJDK";
                    if (verWithJdk.startsWith(nextPrefix)) {
                        return verWithJdk.substring(nextPrefix.length()).trim();
                    }
                }
            }
        }
        catch (ExecutionException e) {
            return null;
        }
        return null;
    }

    @Override
    public String getBinPath(Sdk sdk) {
        return sdk.getHomePath() + "/bin";
    }

    @Override
    public String getToolsPath(Sdk sdk) {
        return null;
    }

    @Override
    public void setupCommandLine(@NotNull GeneralCommandLine commandLine, @NotNull Sdk sdk) {
        if (new File(sdk.getHomePath(), "bin/java.exe").exists()) {
            commandLine.setExePath(sdk.getHomePath() + "/bin/ikvm.exe");
        }
    }
}
