package consulo.ikvm.psi.stubBuilding.psi;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetPsiSearcher;
import consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.dotnet.resolve.SimpleTypeResolveResult;

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
}
