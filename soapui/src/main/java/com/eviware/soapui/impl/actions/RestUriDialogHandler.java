package com.eviware.soapui.impl.actions;

import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormField;
import com.eviware.x.impl.swing.JTextFieldFormField;

import javax.swing.AbstractAction;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Constructs dialogs for inputting REST URIs and manages state in an open dialog.
 */
public class RestUriDialogHandler
{
	private KeyListener initialKeyListener;
	private MouseListener initialMouseListener;
	private Font originalFont;
	private boolean defaultURIReplaced;
	private JUndoableTextField textField;
	private XFormDialog dialog;
	private String uriLabelKey;
	private String exampleUri;


	public XFormDialog buildDialog( MessageSupport messages, AbstractAction actionToBeAdded )
	{
		XFormDialogBuilder newDialogBuilder = XFormFactory.createDialogBuilder( messages.get( "Title" ) );
		XForm form = newDialogBuilder.createForm( "" );
		uriLabelKey = messages.get( "Form.URI.Label" );
		exampleUri = messages.get( "Form.Example.URI" );
		form.addTextField( uriLabelKey, messages.get( "Form.URI.Description" ), XForm.FieldType.TEXT );

		ActionList actions = newDialogBuilder.buildOkCancelHelpActions( HelpUrls.NEWRESTPROJECT_HELP_URL );

		if( actionToBeAdded != null )
		{
			actions.addAction( actionToBeAdded );
		}

		dialog = newDialogBuilder.buildDialog( actions, messages.get( "Description" ), UISupport.TOOL_ICON );
		dialog.setValue( uriLabelKey, exampleUri );
		XFormField uriField = dialog.getFormField( uriLabelKey );

		if( uriField instanceof JTextFieldFormField )
		{
			defaultURIReplaced = false;
			textField = ( ( JTextFieldFormField )uriField ).getComponent();
			textField.requestFocus();
			originalFont = textField.getFont();
			textField.setFont( originalFont.deriveFont( Font.ITALIC ) );
			textField.setForeground( new Color( 170, 170, 170 ) );
			addListenersToTextField();
		}
		return dialog;
	}

	public void resetUriField()
	{
		if( !defaultURIReplaced && textField != null )
		{
			try
			{
				defaultURIReplaced = true;
				textField.setText( "" );
				textField.setFont( originalFont );
				textField.setForeground( Color.BLACK );
			} finally
			{
				if( initialKeyListener != null )
				{
					textField.removeKeyListener( initialKeyListener );
				}
				if( initialMouseListener != null )
				{
					textField.removeMouseListener( initialMouseListener );
				}
			}
		}

	}

	private void addListenersToTextField()
	{
		initialKeyListener = new KeyAdapter()
		{
			@Override
			public void keyPressed( KeyEvent e )
			{
				resetUriField();
			}
		};
		textField.addKeyListener( initialKeyListener );
		initialMouseListener = new MouseAdapter()
		{
			@Override
			public void mouseClicked( MouseEvent e )
			{
				resetUriField();
			}
		};
		textField.addMouseListener( initialMouseListener );
	}

	public String getUri()
	{
		if (dialog.getReturnValue() != XFormDialog.OK_OPTION)
		{
			return null;
		}
		String uri = dialog.getValue( uriLabelKey ).trim();
		return uri.equals(exampleUri) ? "" : uri;
	}
}
