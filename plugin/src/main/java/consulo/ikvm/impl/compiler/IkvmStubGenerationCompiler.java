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

package consulo.ikvm.impl.compiler;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ReadAction;
import consulo.compiler.CompileContext;
import consulo.compiler.SourceProcessingCompiler;
import consulo.compiler.TimestampValidityState;
import consulo.compiler.ValidityState;
import consulo.compiler.scope.CompileScope;
import consulo.compiler.util.CompilerUtil;
import consulo.content.bundle.Sdk;
import consulo.dotnet.DotNetTarget;
import consulo.dotnet.compiler.DotNetCompilerUtil;
import consulo.dotnet.psi.DotNetMemberOwner;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.ikvm.bundle.IkvmBundleType;
import consulo.ikvm.impl.psi.stubBuilding.JavaClassStubBuilder;
import consulo.ikvm.impl.psi.stubBuilding.StubBuilder;
import consulo.ikvm.module.extension.IkvmModuleExtension;
import consulo.ikvm.module.extension.IkvmModuleExtensionUtil;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.layer.orderEntry.ModuleExtensionWithSdkOrderEntry;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.project.Project;
import consulo.util.lang.Pair;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.archive.ArchiveVfsUtil;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.virtualFileSystem.util.VirtualFileVisitor;
import org.jspecify.annotations.Nullable;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 09.05.14
 */
@ExtensionImpl
public class IkvmStubGenerationCompiler implements SourceProcessingCompiler {
    private static final Logger LOG = Logger.getInstance(IkvmStubGenerationCompiler.class);

    public static class Item implements ProcessingItem {
        private final Path myFile;
        private final Module myAffectedModule;

        public Item(Path file, Module affectedModule) {
            myFile = file;
            myAffectedModule = affectedModule;
        }

        @Override
        public Path getFile() {
            return myFile;
        }

        @Override
        public @Nullable ValidityState getValidityState() {
            return new TimestampValidityState(CompilerUtil.lastModified(myFile));
        }

        public Module getAffectedModule() {
            return myAffectedModule;
        }
    }

    @Override
    public ProcessingItem[] getProcessingItems(CompileContext compileContext) {
        Module[] affectedModules = compileContext.getCompileScope().getAffectedModules();
        List<ProcessingItem> items = new ArrayList<>();
        for (Module affectedModule : affectedModules) {
            IkvmModuleExtension extension = affectedModule.getExtension(IkvmModuleExtension.class);
            if (extension == null) {
                continue;
            }

            Set<File> files = ReadAction.compute(
                () -> DotNetCompilerUtil.collectDependencies(affectedModule, DotNetTarget.LIBRARY, true, DotNetCompilerUtil.ACCEPT_ALL)
            );
            for (File file : files) {
                items.add(new Item(file.toPath(), affectedModule));
            }
        }
        return items.isEmpty() ? ProcessingItem.EMPTY_ARRAY : items.toArray(new ProcessingItem[items.size()]);
    }

    @Override
    public ProcessingItem[] process(CompileContext compileContext, ProcessingItem[] processingItems) {
        for (ProcessingItem p : processingItems) {
            Item item = (Item) p;

            List<DotNetTypeDeclaration> typeDeclarations = collectTypes(compileContext.getProject(), item.getFile());
            for (DotNetTypeDeclaration typeDeclaration : typeDeclarations) {
                if (!ReadAction.compute(() -> typeDeclaration.hasModifier(DotNetModifier.PUBLIC))) {
                    continue;
                }
                String typeName = ReadAction.compute(typeDeclaration::getPresentableQName);

                compileContext.getProgressIndicator().setText(LocalizeValue.localizeTODO("Processing: " + typeName));

                Pair<String, byte[]> build = ReadAction.compute(() -> {
                    JavaClassStubBuilder classStubBuilder = StubBuilder.build(typeDeclaration);
                    if (classStubBuilder == null) {
                        return null;
                    }
                    return Pair.create(classStubBuilder.getQualifiedName(), classStubBuilder.buildToBytecode());
                });

                if (build == null) {
                    continue;
                }
                String qualifiedName = build.getFirst();

                Path stubFile = IkvmModuleExtensionUtil.getStubsPath(item.getAffectedModule()).resolve(qualifiedName.replace(".", "/") + ".class");

                try {
                    Files.createDirectories(stubFile.getParent());
                    Files.write(stubFile, build.getSecond());
                }
                catch (IOException e) {
                    LOG.error(e);
                }
            }
        }
        return processingItems;
    }

    private List<DotNetTypeDeclaration> collectTypes(Project project, Path file) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByNioFile(file);
        if (virtualFile == null) {
            return Collections.emptyList();
        }

        VirtualFile archiveRootForLocalFile = ArchiveVfsUtil.getArchiveRootForLocalFile(virtualFile);
        if (archiveRootForLocalFile == null) {
            return Collections.emptyList();
        }

        List<OrderEntry> orderEntriesForFile = ProjectFileIndex.getInstance(project).getOrderEntriesForFile(archiveRootForLocalFile);
        for (OrderEntry orderEntry : orderEntriesForFile) {
            if (orderEntry instanceof ModuleExtensionWithSdkOrderEntry sdkOrderEntry) {
                Sdk sdk = sdkOrderEntry.getSdk();
                if (sdk == null) {
                    continue;
                }
                if (sdk.getSdkType() instanceof IkvmBundleType) {
                    return Collections.emptyList();
                }
            }
        }

        PsiManager psiManager = PsiManager.getInstance(project);
        List<DotNetTypeDeclaration> list = new ArrayList<>();
        VirtualFileUtil.visitChildrenRecursively(archiveRootForLocalFile, new VirtualFileVisitor<Void>() {
            @Override
            public boolean visitFile(VirtualFile file) {
                ReadAction.run(() -> {
                    PsiFile psiFile = psiManager.findFile(file);
                    if (!(psiFile instanceof DotNetMemberOwner)) {
                        return;
                    }

                    list.addAll(PsiTreeUtil.findChildrenOfType(psiFile, DotNetTypeDeclaration.class));
                });
                return true;
            }
        });
        return list;
    }

    @Override
    public String getDescription() {
        return "IkvmStubGenerationCompiler";
    }

    @Override
    public boolean validateConfiguration(CompileScope compileScope) {
        return true;
    }

    @Override
    public ValidityState createValidityState(DataInput dataInput) throws IOException {
        return TimestampValidityState.load(dataInput);
    }
}
