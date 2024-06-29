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
import consulo.content.bundle.SdkModel;
import consulo.ide.setting.ShowSettingsUtil;
import consulo.ikvm.module.extension.IkvmMutableModuleExtension;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.module.ui.awt.SdkComboBox;
import consulo.module.ui.extension.ModuleExtensionSdkBoxBuilder;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.awt.ColoredListCellRenderer;
import consulo.ui.ex.awt.ComboBox;
import consulo.ui.ex.awt.LabeledComponent;
import consulo.ui.ex.awt.VerticalFlowLayout;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author VISTALL
 * @since 12.05.14
 */
public class IkvmModuleExtensionPanel extends JPanel
{
	private SdkComboBox myJavaSdkComboBox;
	private ComboBox myLanguageLevelComboBox;

	private IkvmMutableModuleExtension<?> myModuleExtension;

	@RequiredUIAccess
	public IkvmModuleExtensionPanel(IkvmMutableModuleExtension<?> moduleExtension, Runnable classpathUpdater, boolean supportSdkPanel)
	{
		super(new VerticalFlowLayout());
		myModuleExtension = moduleExtension;

		if(supportSdkPanel)
		{
			add(ModuleExtensionSdkBoxBuilder.createAndDefine(myModuleExtension, classpathUpdater).build());
		}

		final SdkModel projectSdksModel = ShowSettingsUtil.getInstance().getSdksModel();

		myJavaSdkComboBox = new SdkComboBox(projectSdksModel, sdkTypeId -> sdkTypeId == JavaSdkType.getDefaultJavaSdkType(), true);

		final String javaCompilerSdkName = myModuleExtension.getJavaSdkName();
		if(javaCompilerSdkName == null)
		{
			myJavaSdkComboBox.setSelectedNoneSdk();
		}
		else
		{
			myJavaSdkComboBox.setSelectedSdk(javaCompilerSdkName);
		}

		myJavaSdkComboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				myModuleExtension.setSdkForCompilation(myJavaSdkComboBox.getSelectedSdkName());
			}
		});

		myLanguageLevelComboBox = new ComboBox();
		myLanguageLevelComboBox.setRenderer(new ColoredListCellRenderer()
		{
			@Override
			protected void customizeCellRenderer(@Nonnull JList jList, Object value, int i, boolean b, boolean b1)
			{
				if(value instanceof LanguageLevel)
				{
					final LanguageLevel languageLevel = (LanguageLevel) value;
					append(languageLevel.getDescription().get());
				}
			}
		});

		for(LanguageLevel languageLevel : LanguageLevel.values())
		{
			myLanguageLevelComboBox.addItem(languageLevel);
		}

		final MutableModuleInheritableNamedPointer<LanguageLevel> inheritableLanguageLevel = myModuleExtension.getInheritableLanguageLevel();

		myLanguageLevelComboBox.setSelectedItem(inheritableLanguageLevel.get());

		myLanguageLevelComboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				final Object selectedItem = myLanguageLevelComboBox.getSelectedItem();
				if(selectedItem instanceof LanguageLevel)
				{
					inheritableLanguageLevel.set(null, ((LanguageLevel) selectedItem).getName());
				}
				else
				{
					inheritableLanguageLevel.set(selectedItem.toString(), null);
				}
			}
		});

		add(LabeledComponent.left(myJavaSdkComboBox, "Java SDK"));
		add(LabeledComponent.left(myLanguageLevelComboBox, "Language Level"));
	}
}
