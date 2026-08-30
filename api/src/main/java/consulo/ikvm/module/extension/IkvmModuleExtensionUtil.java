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

package consulo.ikvm.module.extension;

import com.intellij.java.indexing.search.searches.AllClassesSearch;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.util.PsiMethodUtil;
import consulo.application.util.query.Query;
import consulo.compiler.ModuleChunk;
import consulo.container.boot.ContainerPathManager;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.bundle.Sdk;
import consulo.ikvm.bundle.IkvmBundleType;
import consulo.ikvm.psi.IkvmJavaClassAsDotNetTypeElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.OrderedSet;
import consulo.util.io.URLUtil;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 25.09.14
 */
public class IkvmModuleExtensionUtil {
    public static PsiElement[] buildEntryPoints(Module module) {
        Query<PsiClass> search = AllClassesSearch.search(GlobalSearchScope.moduleScope(module), module.getProject());

        List<PsiElement> list = new ArrayList<>();
        search.forEach(psiClass -> {
            if (PsiMethodUtil.hasMainMethod(psiClass)) {
                list.add(new IkvmJavaClassAsDotNetTypeElement(psiClass));
            }
            return true;
        });
        return ContainerUtil.toArray(list, PsiElement.ARRAY_FACTORY);
    }

    public static Path getStubsPath(Module module) {
        return Path.of(ContainerPathManager.get().getSystemPath(), "ikvm-stubs", module.getName() + "@" + module.getModuleDirUrl().hashCode());
    }

    public static Set<Path> getCompilationClasspath(IkvmModuleExtension<?> extension, ModuleChunk moduleChunk) {
        Sdk sdkForCompilation = extension.getSdkForCompilation();
        if (sdkForCompilation == null) {
            return Set.of();
        }

        Set<Path> files = new OrderedSet<>();
        for (String url : sdkForCompilation.getRootProvider().getUrls(BinariesOrderRootType.ID)) {
            String path = VirtualFileUtil.urlToPath(url);
            int archiveSeparatorIndex = path.indexOf(URLUtil.ARCHIVE_SEPARATOR);
            if (archiveSeparatorIndex >= 0) {
                path = path.substring(0, archiveSeparatorIndex);
            }
            files.add(Path.of(path));
        }

        Path stubsPath = getStubsPath(extension.getModule());
        if (Files.exists(stubsPath)) {
            files.add(stubsPath);
        }

        files.addAll(moduleChunk.getCompilationClasspathFiles(IkvmBundleType.getInstance()));
        return files;
    }
}
