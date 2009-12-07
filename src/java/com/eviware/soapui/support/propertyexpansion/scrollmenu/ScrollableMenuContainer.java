package com.eviware.soapui.support.propertyexpansion.scrollmenu;

import javax.swing.Action;
import javax.swing.JMenuItem;

public interface ScrollableMenuContainer
{
	public JMenuItem add( JMenuItem menuItem );

	public JMenuItem addHeader( JMenuItem header );

	public JMenuItem addHeader( Action action );

	public JMenuItem addFooter( JMenuItem menuItem );

	public JMenuItem addFooter( Action action );

	public void removeAll();
}