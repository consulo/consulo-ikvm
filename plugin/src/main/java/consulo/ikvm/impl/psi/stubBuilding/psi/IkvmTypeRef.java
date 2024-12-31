package consulo.ikvm.impl.psi.stubBuilding.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetPsiSearcher;
import consulo.dotnet.psi.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.dotnet.psi.resolve.SimpleTypeResolveResult;
import consulo.language.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 06.07.14
 */
public class IkvmTypeRef extends DotNetTypeRefWithCachedResult
{
	private PsiElement myNavTarget;
	private final String myQualifiedName;

	public IkvmTypeRef(PsiElement navTarget, String qualifiedName)
	{
		super(navTarget.getProject(), navTarget.getResolveScope());
		myNavTarget = navTarget;
		myQualifiedName = qualifiedName;
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(myNavTarget.getProject()).findType(myQualifiedName, myNavTarget.getResolveScope());
		if(type != null)
		{
			return new SimpleTypeResolveResult(type);
		}
		return DotNetTypeResolveResult.EMPTY;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String toString()
	{
		return myQualifiedName;
	}

	@Nonnull
	@Override
	public String getVmQName()
	{
		return myQualifiedName;
	}
}
