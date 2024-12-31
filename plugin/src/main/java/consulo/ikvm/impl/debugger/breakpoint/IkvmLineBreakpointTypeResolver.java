/*
 * Copyright 2013-2016 must-be.org
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

package consulo.ikvm.impl.debugger.breakpoint;

import com.intellij.java.debugger.impl.ui.breakpoints.JavaLineBreakpointType;
import com.intellij.java.language.impl.JavaFileType;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.dotnet.debugger.impl.breakpoint.DotNetLineBreakpointType;
import consulo.execution.debug.breakpoint.XLineBreakpointType;
import consulo.execution.debug.breakpoint.XLineBreakpointTypeResolver;
import consulo.ikvm.module.extension.IkvmModuleExtension;
import consulo.java.debugger.impl.JavaLineBreakpointTypeResolver;
import consulo.language.util.ModuleUtilCore;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 13-May-16
 */
@ExtensionImpl
public class IkvmLineBreakpointTypeResolver implements XLineBreakpointTypeResolver
{
	private static JavaLineBreakpointTypeResolver ourJavaResolver = new JavaLineBreakpointTypeResolver();

	@RequiredReadAction
	@Nullable
	@Override
	public XLineBreakpointType<?> resolveBreakpointType(@NotNull Project project, @NotNull VirtualFile virtualFile, int line)
	{
		XLineBreakpointType<?> breakpointType = ourJavaResolver.resolveBreakpointType(project, virtualFile, line);
		if(breakpointType == JavaLineBreakpointType.getInstance())
		{
			IkvmModuleExtension extension = ModuleUtilCore.getExtension(project, virtualFile, IkvmModuleExtension.class);
			if(extension == null)
			{
				return null;
			}
			return DotNetLineBreakpointType.getInstance();
		}
		return null;
	}

	@Nonnull
	@Override
	public FileType getFileType()
	{
		return JavaFileType.INSTANCE;
	}
}
