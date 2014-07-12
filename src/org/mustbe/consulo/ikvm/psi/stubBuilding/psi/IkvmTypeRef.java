package org.mustbe.consulo.ikvm.psi.stubBuilding.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 06.07.14
 */
public class IkvmTypeRef extends DotNetTypeRef.Adapter
{
	private final String myQualifiedName;

	public IkvmTypeRef(String qualifiedName)
	{
		myQualifiedName = qualifiedName;
	}

	@NotNull
	@Override
	public String getQualifiedText()
	{
		return myQualifiedName;
	}

	@Nullable
	@Override
	public PsiElement resolve(@NotNull PsiElement scope)
	{
		return super.resolve(scope);
	}
}
