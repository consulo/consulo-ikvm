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

import org.consulo.module.extension.MutableModuleExtensionWithSdk;
import org.jetbrains.annotations.Nullable;
import consulo.ikvm.IkvmModuleExtension;
import org.mustbe.consulo.java.module.extension.JavaMutableModuleExtension;
import com.intellij.openapi.projectRoots.Sdk;

/**
 * @author VISTALL
 * @since 12.05.14
 */
public interface IkvmMutableModuleExtension<T extends IkvmModuleExtension<T>> extends IkvmModuleExtension<T>, MutableModuleExtensionWithSdk<T>, JavaMutableModuleExtension<T>
{
	void setSdkForCompilation(@Nullable Sdk sdkForCompilation);

	void setSdkForCompilation(@Nullable String sdkForCompilation);
}
