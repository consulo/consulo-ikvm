/*
 * Copyright 2013-2015 must-be.org
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

package consulo.ikvm.impl.representation;

import com.intellij.java.language.impl.JavaFileType;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.ikvm.impl.psi.stubBuilding.JavaClassStubBuilder;
import consulo.ikvm.impl.psi.stubBuilding.StubBuilder;
import consulo.msil.lang.psi.MsilFile;
import consulo.msil.representation.MsilFileRepresentationProvider;
import consulo.virtualFileSystem.fileType.FileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 08.07.2015
 */
@ExtensionImpl
public class JavaRepresentationProvider implements MsilFileRepresentationProvider
{
	@Nullable
	@Override
	@RequiredReadAction
	public String getRepresentFileName(@NotNull MsilFile msilFile)
	{
		return msilFile.getContainingFile().getVirtualFile().getNameWithoutExtension();
	}

	@Nonnull
	@Override
	public CharSequence buildContent(String fileName, @Nonnull MsilFile msilFile)
	{
		DotNetNamedElement[] members = msilFile.getMembers();
		StringBuilder builder = new StringBuilder();
		for(DotNetNamedElement member : members)
		{
			JavaClassStubBuilder build = StubBuilder.build((DotNetTypeDeclaration) member);
			if(build == null)
			{
				continue;
			}
			build.buildToText(builder, null);
			builder.append("\n");
		}
		return builder.toString();
	}

	@NotNull
	@Override
	public FileType getFileType()
	{
		return JavaFileType.INSTANCE;
	}
}
