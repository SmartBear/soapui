/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.actions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.eviware.soapui.support.UISupport;

public class ShowSystemPropertiesAction extends AbstractAction
{
	public ShowSystemPropertiesAction()
	{
		super( "System Properties" );
		putValue( Action.SHORT_DESCRIPTION, "Shows the current systems properties" );
	}

	public void actionPerformed( ActionEvent e )
	{
		StringBuffer buffer = new StringBuffer();
		Properties properties = System.getProperties();

		List<String> keys = new ArrayList<String>();
		for( Object key : properties.keySet() )
			keys.add( key.toString() );

		Collections.sort( keys );

		String lastKey = null;

		for( String key : keys )
		{
			if( lastKey != null )
			{
				if( !key.startsWith( lastKey ) )
					buffer.append( "\r\n" );
			}

			int ix = key.indexOf( '.' );
			lastKey = ix == -1 ? key : key.substring( 0, ix );

			buffer.append( key ).append( '=' ).append( properties.get( key ) ).append( "\r\n" );
		}

		UISupport.showExtendedInfo( "System Properties", "Current system properties", "<html><body><pre><font size=-1>"
				+ buffer.toString() + "</font></pre></body></html>", new Dimension( 600, 400 ) );
	}
}
