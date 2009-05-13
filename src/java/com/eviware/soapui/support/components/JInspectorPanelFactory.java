/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.components;

import javax.swing.JComponent;

public class JInspectorPanelFactory
{

	public static Class<? extends JInspectorPanel> inspectorPanelClass = JInspectorPanelImpl.class;

	public static JInspectorPanel build( JComponent contentComponent )
	{
		try
		{
			return inspectorPanelClass.getConstructor( JComponent.class ).newInstance( contentComponent );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return null;
		}
	}

	public static JInspectorPanel build( JComponent contentComponent, int orientation )
	{
		try
		{
			return inspectorPanelClass.getConstructor( JComponent.class, int.class ).newInstance( contentComponent,
					orientation );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return null;
		}
	}
}
