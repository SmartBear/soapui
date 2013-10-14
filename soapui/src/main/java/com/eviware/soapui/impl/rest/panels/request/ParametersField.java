package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTableModel;
import com.eviware.soapui.impl.rest.support.RestUtils;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A component that displays matrix and query string parameters for a REST request and provides a popup to edit them.
 */
class ParametersField extends JPanel
{

	private final RestRequestInterface request;
	private final JLabel textLabel;
	private final JTextField textField;
	private Component popupComponent;
	private boolean textFieldClicked;

	ParametersField( RestRequestInterface request )
	{
		this.request = request;
		textLabel = new JLabel( "Parameters" );
		String paramsString = RestUtils.makeSuffixParameterString( request );
		textField = new JTextField( paramsString );
		//textField.setEditable( false );
		setToolTipText( paramsString );
		super.setLayout( new BorderLayout() );
		super.add( textLabel, BorderLayout.NORTH );
		super.add( textField, BorderLayout.SOUTH );
		addListeners();

	}

	private void addListeners()
	{
		textField.addMouseListener( new MouseAdapter()
		{

			@Override
			public void mouseReleased( MouseEvent e )
			{
				textFieldClicked = true;
			}


		} );
		textField.addCaretListener( new CaretListener()
		{
			@Override
			public void caretUpdate( CaretEvent e )
			{
				System.out.println(e);
				//TODO: add logic to avoid showing popup twice when you click at the beginning or end of the field
				if( textFieldClicked || caretIsInMiddleOfTextField( e ))
				{
					ParameterFinder finder = new ParameterFinder( textField.getText() );
					openPopup( finder.findParameterAt( e.getDot() ) );
					// this is to prevent direct edits of the text field
					textLabel.requestFocus();
					textFieldClicked = false;
				}
			}

			private boolean caretIsInMiddleOfTextField( CaretEvent e )
			{
				int textEndLocation = textField.getText().length();
				return e.getDot() > 0 && e.getDot() < textEndLocation;
			}
		} );

	}

	public String getText()
	{
		return textField.getText();
	}

	public void setText( String text )
	{
		textField.setText( text );
		setToolTipText( text );
	}

	@Override
	public void setToolTipText( String text )
	{
		super.setToolTipText( text );
		textLabel.setToolTipText( text );
		textField.setToolTipText( text );
	}

	private void openPopup( final String selectedParameter )
	{
		final RestParamsTable restParamsTable = new RestParamsTable( request.getParams(), false, new RestParamsTableModel(
				request.getParams(), RestParamsTableModel.Mode.MINIMAL ),
				NewRestResourceActionBase.ParamLocation.METHOD, true, true );
		restParamsTable.addKeyListener( new KeyAdapter()
		{
			@Override
			public void keyPressed( KeyEvent e )
			{
				if( e.getKeyChar() == KeyEvent.VK_ESCAPE )
				{
					closePopup();
				}
			}
		} );
		showParametersTableInWindow( restParamsTable, selectedParameter );
	}

	private void showParametersTableInWindow( RestParamsTable restParamsTable, String selectedParameter )
	{
		popupComponent = new PopupWindow( restParamsTable );
		PopupWindow popupWindow = new PopupWindow( restParamsTable );
		popupWindow.pack();
		restParamsTable.focusParameter( selectedParameter );
		moveWindowBelowTextField( popupWindow );
		popupWindow.setModal( true );
		popupWindow.setVisible( true );
	}

	private void moveWindowBelowTextField( PopupWindow popupWindow )
	{
		try
		{
			Point textFieldLocation = textField.getLocationOnScreen();
			popupWindow.setLocation( textFieldLocation.x, textFieldLocation.y + textField.getHeight() );
		} catch (IllegalComponentStateException ignore)
		{
			 // this will happen when the desktop panel is being closed
		}
	}

	public void closePopup()
	{
		if( popupComponent != null )
		{
			popupComponent.setVisible( false );
			popupComponent = null;
		}
	}

	private class PopupWindow extends JDialog
	{

		private PopupWindow( RestParamsTable restParamsTable )
		{
			super( SoapUI.getFrame() );
			getContentPane().setLayout( new BorderLayout() );
			JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
			JButton closeButton = new JButton( "Close" );
			closeButton.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( ActionEvent e )
				{
					setVisible( false );
					dispose();
				}
			} );
			buttonPanel.add( closeButton );
			getContentPane().add( restParamsTable, BorderLayout.CENTER );
			getContentPane().add( buttonPanel, BorderLayout.SOUTH );
		}
	}

}
