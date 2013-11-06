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
package com.eviware.soapui.support.xml.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.fife.ui.rtextarea.RTextScrollPane;

import com.eviware.soapui.support.UISupport;

public class EnableLineNumbersAction extends AbstractAction
{
	private final RTextScrollPane editorScrollPane;

	public EnableLineNumbersAction( RTextScrollPane editorScrollPane, String title )
	{
		super( title );
		this.editorScrollPane = editorScrollPane;
		if( UISupport.isMac() )
		{
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "ctrl L" ) );
		}
		else
		{
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "alt L" ) );
		}
	}

	@Override
	public void actionPerformed( ActionEvent e )
	{
		editorScrollPane.setLineNumbersEnabled( !editorScrollPane.getLineNumbersEnabled() );
	}

}
