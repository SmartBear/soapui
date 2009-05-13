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

package com.eviware.soapui.impl.wsdl.loadtest;

import java.awt.Color;

import com.eviware.soapui.model.ModelItem;

/**
 * Utility for getting unique colors (for graphs, etc)
 * 
 * @author Ole.Matzura
 */

public class ColorPalette
{
	public static Color getColor( Object object )
	{
		if( object instanceof ModelItem )
			return new Color( ( ( ModelItem )object ).getName().hashCode() ).brighter();
		else
			return new Color( object.hashCode() );
	}
}
