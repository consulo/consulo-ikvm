/**
 * @author VISTALL
 * @since 27/01/2023
 */
module consulo.ikvm
{
	requires consulo.ikvm.api;
	requires consulo.dotnet.api;
	requires consulo.dotnet.psi.api;
	requires consulo.dotnet.debugger.impl;
	requires consulo.dotnet.impl;
	requires consulo.dotnet.msil.api;
	requires consulo.dotnet.msil.impl;
	requires consulo.java.language.api;
	requires consulo.java.language.impl;
	requires com.intellij.xml;
	requires consulo.internal.dotnet.msil.decompiler;
}