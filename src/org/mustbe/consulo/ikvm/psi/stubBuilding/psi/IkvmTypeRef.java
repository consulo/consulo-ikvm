package org.mustbe.consulo.ikvm.psi.stubBuilding.psi;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

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
}
