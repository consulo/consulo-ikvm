/**
 * @author VISTALL
 * @since 27/01/2023
 */
module consulo.ikvm.api
{
	// platform
	requires transitive consulo.application.api;
	requires transitive consulo.application.content.api;
	requires transitive consulo.compiler.api;
	requires transitive consulo.ide.api;
	requires transitive consulo.language.api;
	requires transitive consulo.language.impl;
	requires transitive consulo.localize.api;
	requires transitive consulo.module.api;
	requires transitive consulo.module.content.api;
	requires transitive consulo.module.ui.api;
	requires transitive consulo.process.api;
	requires transitive consulo.ui.api;
	requires transitive consulo.ui.ex.awt.api;
	requires transitive consulo.util.collection;
	requires transitive consulo.util.io;
	requires transitive consulo.util.lang;
	requires transitive consulo.virtual.file.system.api;

	// plugin-level
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
