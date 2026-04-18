/**
 * @author VISTALL
 * @since 27/01/2023
 */
module consulo.ikvm
{
	// platform
	requires consulo.application.api;
	requires consulo.application.content.api;
	requires consulo.compiler.api;
	requires consulo.container.api;
	requires consulo.execution.debug.api;
	requires consulo.language.api;
	requires consulo.language.impl;
	requires consulo.module.api;
	requires consulo.module.content.api;
	requires consulo.navigation.api;
	requires consulo.project.api;
	requires consulo.util.collection;
	requires consulo.util.io;
	requires consulo.util.lang;
	requires consulo.virtual.file.system.api;

	// plugin-level
	requires consulo.ikvm.api;
	requires consulo.dotnet.api;
	requires consulo.dotnet.psi.api;
	requires consulo.dotnet.debugger.impl;
	requires consulo.dotnet.impl;
	requires consulo.dotnet.msil.api;
	requires consulo.dotnet.msil.impl;
	requires consulo.java.language.api;
	requires consulo.java.language.impl;
	requires consulo.java.debugger.impl;
	requires com.intellij.xml;
	requires consulo.internal.dotnet.msil.decompiler;
	requires asm;
}
