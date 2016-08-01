package consulo.ikvm.psi.stubBuilding.psi;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.consulo.dotnet.resolve.SimpleTypeResolveResult;
import com.intellij.psi.PsiElement;

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
