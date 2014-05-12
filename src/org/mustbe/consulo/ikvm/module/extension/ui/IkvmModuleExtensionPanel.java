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

package org.mustbe.consulo.ikvm.module.extension.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JList;
import javax.swing.JPanel;

import org.consulo.module.extension.MutableModuleInheritableNamedPointer;
import org.consulo.module.extension.ui.ModuleExtensionWithSdkPanel;
import org.mustbe.consulo.ikvm.module.extension.IkvmMutableModuleExtension;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.SdkComboBox;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Condition;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.ui.ColoredListCellRendererWrapper;
import com.intellij.ui.SimpleTextAttributes;

/**
 * @author VISTALL
 * @since 12.05.14
 */
public class IkvmModuleExtensionPanel extends JPanel
{
	private JPanel myRoot;
	private JPanel mySdkPanel;
	private SdkComboBox myJavaSdkComboBox;
	private ComboBox myLanguageLevelComboBox;

	private IkvmMutableModuleExtension<?> myModuleExtension;
	private final Runnable myClasspathUpdater;
	private final boolean mySupportSdkPanel;

	public IkvmModuleExtensionPanel(IkvmMutableModuleExtension<?> moduleExtension, Runnable classpathUpdater, boolean supportSdkPanel)
	{
		myModuleExtension = moduleExtension;
		myClasspathUpdater = classpathUpdater;
		mySupportSdkPanel = supportSdkPanel;
	}

	private void createUIComponents()
	{
		myRoot = this;
		mySdkPanel = mySupportSdkPanel ? new ModuleExtensionWithSdkPanel(myModuleExtension, myClasspathUpdater) : new JPanel();
		mySdkPanel.setVisible(mySupportSdkPanel);

		final ProjectSdksModel projectSdksModel = ProjectStructureConfigurable.getInstance(myModuleExtension.getProject()).getProjectSdksModel();

		myJavaSdkComboBox = new SdkComboBox(projectSdksModel, new Condition<SdkTypeId>()
		{
			@Override
			public boolean value(SdkTypeId sdkTypeId)
			{
				return sdkTypeId == JavaSdk.getInstance();
			}
		}, true);

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
		myLanguageLevelComboBox.setRenderer(new ColoredListCellRendererWrapper<Object>()
		{
			@Override
			protected void doCustomize(JList list, Object value, int index, boolean selected, boolean hasFocus)
			{
				if(value instanceof LanguageLevel)
				{
					final LanguageLevel languageLevel = (LanguageLevel) value;
					append(languageLevel.getShortText(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
					append(" ");
					append(languageLevel.getDescription(), SimpleTextAttributes.GRAY_ATTRIBUTES);
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
	}
}
