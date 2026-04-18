/**
 * @author VISTALL
 * @since 27/01/2023
 */
module consulo.ikvm.microsoft.impl
{
	// platform (non-transitive from api)
	requires consulo.component.api;
	requires consulo.container.api;
	requires consulo.disposer.api;

	// plugin-level
	requires consulo.ikvm.api;
	requires consulo.dotnet.microsoft;
	requires com.intellij.xml;
	requires consulo.dotnet.impl;

	// TODO remove
	requires java.desktop;
}
