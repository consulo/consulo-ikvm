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

package consulo.ikvm.debugger.breakpoint;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.debugger.ui.breakpoints.JavaLineBreakpointType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.debugger.breakpoint.DotNetLineBreakpointType;
import consulo.ikvm.IkvmModuleExtension;
import consulo.java.debugger.JavaLineBreakpointTypeResolver;
import consulo.xdebugger.breakpoints.XLineBreakpointTypeResolver;

/**
 * @author VISTALL
 * @since 13-May-16
 */
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
}
