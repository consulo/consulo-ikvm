package consulo.ikvm.impl.compiler;

import com.intellij.java.compiler.impl.javaCompiler.JavaCompiler;
import consulo.annotation.component.ExtensionImpl;
import consulo.compiler.Compiler;
import consulo.compiler.CompilerSorter;
import consulo.compiler.TranslatingCompiler;
import consulo.dotnet.impl.compiler.DotNetCompiler;
import consulo.ikvm.module.extension.IkvmModuleExtension;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 08.05.14
 */
@ExtensionImpl
public class IkvmCompilerSorter implements CompilerSorter
{
	@Override
	public void sort(Chunk<Module> moduleChunk, @NotNull Compiler[] compilers, Class<? extends Compiler> aClass)
	{
		if(aClass == TranslatingCompiler.class)
		{
			boolean needSwap = false;
			for(Module module : moduleChunk.getNodes())
			{
				if(ModuleUtilCore.getExtension(module, IkvmModuleExtension.class) != null)
				{
					needSwap = true;
					break;
				}
			}

			if(needSwap)
			{
				int javaIndex = indexOfViaInstanceOf(compilers, JavaCompiler.class);
				int dotNetIndex = indexOfViaInstanceOf(compilers, DotNetCompiler.class);
				if(javaIndex > dotNetIndex)
				{
					ArrayUtil.swap(compilers, javaIndex, dotNetIndex);
				}
			}
		}
	}

	private static <T> int indexOfViaInstanceOf(T[] array, Class<? extends T> clazz)
	{
		for(int i = 0; i < array.length; i++)
		{
			T t = array[i];
			if(t.getClass().isAssignableFrom(clazz))
			{
				return i;
			}
		}
		return -1;
	}
}
