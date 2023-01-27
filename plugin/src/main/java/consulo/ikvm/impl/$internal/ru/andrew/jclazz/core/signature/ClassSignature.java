package consulo.ikvm.impl.$internal.ru.andrew.jclazz.core.signature;

import consulo.util.collection.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/*
ClassSignature:
   <FormalTypeParameter+> ClassTypeSignature ClassTypeSignature*
   optional               superclass         interfaces
 */
public class ClassSignature
{
	private ClassTypeSignature superClass;
	private ClassTypeSignature[] interfaces = ClassTypeSignature.EMPTY_ARRAY;
	private FormalTypeParameter[] typeParameters = FormalTypeParameter.EMPTY_ARRAY;

	public ClassSignature(String signature)
	{
		StringBuffer sb = new StringBuffer(signature);
		if(signature.charAt(0) == '<')
		{
			sb.deleteCharAt(0);
			List<FormalTypeParameter> ftps = new SmartList<FormalTypeParameter>();
			while(sb.charAt(0) != '>')
			{
				ftps.add(FormalTypeParameter.parse(sb));
			}
			typeParameters = new FormalTypeParameter[ftps.size()];
			ftps.toArray(typeParameters);
			sb.deleteCharAt(0);
		}

		// Loading super class
		if(sb.length() > 0)
		{
			superClass = ClassTypeSignature.parse(sb);
		}
		List<ClassTypeSignature> intfs = new SmartList<ClassTypeSignature>();
		while(sb.length() > 0)
		{
			intfs.add(ClassTypeSignature.parse(sb));
		}
		interfaces = new ClassTypeSignature[intfs.size()];
		intfs.toArray(interfaces);
	}

	@Nullable
	public ClassTypeSignature getSuperClass()
	{
		return superClass;
	}

	@NotNull
	public ClassTypeSignature[] getInterfaces()
	{
		return interfaces;
	}

	@NotNull
	public FormalTypeParameter[] getTypeParameters()
	{
		return typeParameters;
	}
}
