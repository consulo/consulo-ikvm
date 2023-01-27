/**
 * @author VISTALL
 * @since 27/01/2023
 */
module consulo.ikvm.api
{
	requires transitive consulo.ide.api;
	requires transitive consulo.dotnet.api;
	requires transitive consulo.dotnet.psi.api;
	requires transitive consulo.java;
	requires transitive com.intellij.xml;

	exports consulo.ikvm;
	exports consulo.ikvm.bundle;
	exports consulo.ikvm.compiler;
	exports consulo.ikvm.module.extension;
	exports consulo.ikvm.module.extension.ui;
	exports consulo.ikvm.psi;

	// TODO remove
	requires java.desktop;
	requires consulo.dotnet.impl;
}