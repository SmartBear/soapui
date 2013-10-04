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

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import com.eviware.soapui.support.UISupport;

public class GoToLineAction extends AbstractAction
{
	private final RSyntaxTextArea editArea;

	public GoToLineAction( RSyntaxTextArea editArea, String title )
	{
		super( title );
		this.editArea = editArea;
		putValue( Action.SHORT_DESCRIPTION, "Moves the caret to the specified line" );
		if( UISupport.isMac() )
		{
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "control meta L" ) );
		}
		else
		{
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "ctrl alt L" ) );
		}

	}

	public void actionPerformed( ActionEvent e )
	{
		String line = UISupport.prompt( "Enter line-number to (1.." + ( editArea.getLineCount() ) + ")", "Go To Line",
				String.valueOf( editArea.getCaretLineNumber() + 1 ) );

		if( line != null )
		{
			try
			{
				int ln = Integer.parseInt( line ) - 1;

				if( ln < 0 )
				{
					ln = 0;
				}

				if( ln >= editArea.getLineCount() )
				{
					ln = editArea.getLineCount() - 1;
				}

				editArea.scrollRectToVisible( editArea.modelToView( editArea.getLineStartOffset( ln ) ) );
				editArea.setCaretPosition( editArea.getLineStartOffset( ln ) );
			}
			catch( Exception e1 )
			{
			}
		}
	}
}
