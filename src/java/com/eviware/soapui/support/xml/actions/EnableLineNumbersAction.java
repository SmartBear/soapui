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
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "ctrl alt L" ) );
		}
	}

	@Override
	public void actionPerformed( ActionEvent e )
	{
		editorScrollPane.setLineNumbersEnabled( !editorScrollPane.getLineNumbersEnabled() );
	}

}
