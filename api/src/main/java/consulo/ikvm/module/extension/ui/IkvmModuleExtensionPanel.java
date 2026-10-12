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

package consulo.ikvm.module.extension.ui;

import com.intellij.java.language.LanguageLevel;
import com.intellij.java.language.projectRoots.JavaSdkType;
import consulo.disposer.Disposable;
import consulo.ide.setting.ShowSettingsUtil;
import consulo.ikvm.module.extension.IkvmMutableModuleExtension;
import consulo.localize.LocalizeValue;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.module.ui.extension.ModuleExtensionBundleBoxBuilder;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.util.FormBuilder;

/**
 * @author VISTALL
 * @since 12.05.14
 */
public final class IkvmModuleExtensionPanel {
    private IkvmModuleExtensionPanel() {
    }

    @RequiredUIAccess
    public static Component create(
        IkvmMutableModuleExtension<?> moduleExtension,
        Disposable uiDisposable,
        Runnable classpathUpdater,
        boolean supportSdkPanel
    ) {
        BundleBox javaSdkBox = BundleBoxBuilder.create(ShowSettingsUtil.getInstance().getSdksModel(), uiDisposable)
            .withSdkTypeFilter(sdkTypeId -> sdkTypeId == JavaSdkType.getDefaultJavaSdkType())
            .withNoneItem()
            .build();

        String javaCompilerSdkName = moduleExtension.getJavaSdkName();
        if (javaCompilerSdkName == null) {
            javaSdkBox.setSelectedNoneBundle();
        }
        else {
            javaSdkBox.setSelectedBundle(javaCompilerSdkName);
        }

        javaSdkBox.getComponent().addValueListener(event -> moduleExtension.setSdkForCompilation(javaSdkBox.getSelectedBundleName()));

        MutableModuleInheritableNamedPointer<LanguageLevel> inheritableLanguageLevel = moduleExtension.getInheritableLanguageLevel();

        ComboBox<LanguageLevel> languageLevelBox = ComboBox.create(LanguageLevel.values());
        languageLevelBox.setTextRenderer(languageLevel -> languageLevel == null ? LocalizeValue.empty() : languageLevel.getDescription());
        languageLevelBox.setValue(inheritableLanguageLevel.get());
        languageLevelBox.addValueListener(event -> {
            LanguageLevel languageLevel = event.getValue();
            if (languageLevel != null) {
                inheritableLanguageLevel.set(null, languageLevel.getName());
            }
        });

        FormBuilder formBuilder = FormBuilder.create();
        formBuilder.addLabeled(LocalizeValue.localizeTODO("Java SDK:"), javaSdkBox.getComponent());
        formBuilder.addLabeled(LocalizeValue.localizeTODO("Language Level:"), languageLevelBox);

        VerticalLayout layout = VerticalLayout.create();
        if (supportSdkPanel) {
            layout.add(ModuleExtensionBundleBoxBuilder.createAndDefine(moduleExtension, uiDisposable, classpathUpdater).build());
        }
        layout.add(formBuilder.build());
        return layout;
    }
}
