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
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "control meta L" ) );
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
