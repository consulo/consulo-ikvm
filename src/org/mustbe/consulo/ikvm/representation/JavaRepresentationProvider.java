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

package org.mustbe.consulo.ikvm.representation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.ikvm.psi.stubBuilding.JavaClassStubBuilder;
import org.mustbe.consulo.ikvm.psi.stubBuilding.StubBuilder;
import org.mustbe.consulo.msil.lang.psi.MsilFile;
import org.mustbe.consulo.msil.representation.MsilFileRepresentationProvider;
import org.mustbe.consulo.msil.representation.MsilFileRepresentationVirtualFile;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.source.PsiJavaFileImpl;

/**
 * @author VISTALL
 * @since 08.07.2015
 */
public class JavaRepresentationProvider implements MsilFileRepresentationProvider
{
	@Nullable
	@Override
	@RequiredReadAction
	public String getRepresentFileName(@NotNull MsilFile msilFile)
	{
		return msilFile.getContainingFile().getVirtualFile().getNameWithoutExtension();
	}

	@NotNull
	@Override
	@RequiredReadAction
	public PsiFile transform(String fileName, @NotNull MsilFile msilFile)
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

		VirtualFile virtualFile = new MsilFileRepresentationVirtualFile(fileName, JavaFileType.INSTANCE, builder);

		SingleRootFileViewProvider viewProvider = new SingleRootFileViewProvider(PsiManager.getInstance(msilFile.getProject()), virtualFile, true);

		final PsiJavaFileImpl file = new PsiJavaFileImpl(viewProvider);

		viewProvider.forceCachedPsi(file);

		((PsiManagerEx) PsiManager.getInstance(msilFile.getProject())).getFileManager().setViewProvider(virtualFile, viewProvider);

		new WriteCommandAction.Simple<Object>(file.getProject(), file)
		{
			@Override
			protected void run() throws Throwable
			{
				CodeStyleManager.getInstance(getProject()).reformat(file);
			}
		}.execute();
		return file;
	}

	@NotNull
	@Override
	public FileType getFileType()
	{
		return JavaFileType.INSTANCE;
	}
}
