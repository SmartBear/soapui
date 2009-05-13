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

package com.eviware.soapui.support.xml.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.log4j.Logger;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.JXEditTextArea;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Formats the XML of a JXmlTextArea
 * 
 * @author Ole.Matzura
 */

public class FormatXmlAction extends AbstractAction
{
	private final static Logger log = Logger.getLogger( FormatXmlAction.class );
	private final JXEditTextArea textArea;

	public FormatXmlAction( JXEditTextArea textArea )
	{
		super( "Format XML" );
		putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/format_request.gif" ) );
		putValue( Action.SHORT_DESCRIPTION, "Pretty-prints the request xml" );
		putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "alt F" ) );
		this.textArea = textArea;
	}

	public void actionPerformed( ActionEvent e )
	{
		try
		{
			textArea.setText( XmlUtils.prettyPrintXml( textArea.getText() ) );
			textArea.setCaretPosition( 0 );
		}
		catch( Exception e1 )
		{
			log.error( e1.getMessage() );
		}
	}
}
