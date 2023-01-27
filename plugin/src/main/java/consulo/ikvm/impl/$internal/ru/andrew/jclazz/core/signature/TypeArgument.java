package consulo.ikvm.impl.$internal.ru.andrew.jclazz.core.signature;

import consulo.util.collection.ArrayFactory;

/*
TypeArgument:
   [+-]opt FieldTypeSignature
   *
 */
public class TypeArgument
{
	public static final TypeArgument[] EMPTY_ARRAY = new TypeArgument[0];

	public static ArrayFactory<TypeArgument> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new TypeArgument[count];

	private char modifier;
	private FieldTypeSignature fieldType;

	private TypeArgument(char modifier, FieldTypeSignature fieldType)
	{
		this.modifier = modifier;
		this.fieldType = fieldType;
	}

	private TypeArgument(char modifier)
	{
		this.modifier = modifier;
	}

	public static TypeArgument parse(StringBuffer sign)
	{
		if(sign.charAt(0) == '*')
		{
			sign.deleteCharAt(0);
			return new TypeArgument('*');
		}

		char mod = 'x';
		if(sign.charAt(0) == '+' || sign.charAt(0) == '-')
		{
			mod = sign.charAt(0);
			sign.deleteCharAt(0);
		}
		FieldTypeSignature fts = FieldTypeSignature.parse(sign);
		return new TypeArgument(mod, fts);
	}

	public char getModifier()
	{
		return modifier;
	}

	public FieldTypeSignature getFieldType()
	{
		return fieldType;
	}
}
