package org.mustbe.consulo.ikvm.compiler;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.compiler.DotNetCompiler;
import org.mustbe.consulo.ikvm.IkvmModuleExtension;
import com.intellij.compiler.impl.javaCompiler.JavaCompiler;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.compiler.Compiler;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Chunk;

/**
 * @author VISTALL
 * @since 08.05.14
 */
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
