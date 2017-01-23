package consulo.ikvm.$internal.ru.andrew.jclazz.core.signature;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayFactory;

/*
ClassTypeSignature:
   L Package/Package.../ SimpleClassTypeSignature (.SimpleClassTypeSignature)*;
                                                  suffix
 */
public class ClassTypeSignature
{
	public static final ClassTypeSignature[] EMPTY_ARRAY = new ClassTypeSignature[0];

	public static ArrayFactory<ClassTypeSignature> ARRAY_FACTORY = new ArrayFactory<ClassTypeSignature>()
	{
		@NotNull
		@Override
		public ClassTypeSignature[] create(int count)
		{
			return count == 0 ? EMPTY_ARRAY : new ClassTypeSignature[count];
		}
	};

	private String pack;
	private SimpleClassTypeSignature cl;
	private SimpleClassTypeSignature[] suffix;

	private ClassTypeSignature(String pack, SimpleClassTypeSignature cl, List suffix)
	{
		this.pack = pack;
		this.cl = cl;
		this.suffix = new SimpleClassTypeSignature[suffix.size()];
		suffix.toArray(this.suffix);
	}

	public static ClassTypeSignature parse(StringBuffer sign)
	{
		if(sign.charAt(0) != 'L')
			throw new RuntimeException("ClassTypeSignature: invalid L");
		sign.deleteCharAt(0);

		// Loading package
		StringBuilder packs = new StringBuilder();
		int ind;
		while((ind = sign.indexOf("/")) != -1)
		{
			String pack = sign.substring(0, ind);
			if((pack.indexOf('<') != -1) || (pack.indexOf('.') != -1) || (pack.indexOf(';') != -1))
			{
				break;
			}
			packs.append(pack).append(".");
			sign.delete(0, ind + 1);
		}
		if(packs.length() > 0)
			packs.deleteCharAt(packs.length() - 1);

		// Loading class
		SimpleClassTypeSignature cl = SimpleClassTypeSignature.parse(sign);

		// Loading suffix
		List<SimpleClassTypeSignature> suf = new ArrayList<SimpleClassTypeSignature>();
		while(sign.charAt(0) != ';')
		{
			sign.deleteCharAt(0);
			suf.add(SimpleClassTypeSignature.parse(sign));
		}
		sign.deleteCharAt(0);

		return new ClassTypeSignature(packs.toString(), cl, suf);
	}

	public String getPackage()
	{
		return pack;
	}

	@NotNull
	public String getClassName()
	{
		if(StringUtil.isEmpty(pack))
		{
			return cl.getName();
		}
		return pack + "." + cl.getName();
	}

	public SimpleClassTypeSignature getClassType()
	{
		return cl;
	}

	public SimpleClassTypeSignature[] getSuffix()
	{
		return suffix;
	}
}
