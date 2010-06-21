package com.eviware.soapui.support.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.eviware.soapui.support.UISupport;

/**
 * Workaround because JOptionPane doesn't work well with ModalFrameUtil; if a
 * JOptionPane is displayed, the modal frame is temporarily hidden.
 * 
 * @author lars
 */
public class ModalFrameDialog
{
	private JFrame frame;
	private JTextField textField;
	private JButton okButton;
	private JButton cancelButton;

	private String retValue = null;

	public static String showInputDialog( Frame parent, String question, String title, String initialValue )
	{
		ModalFrameDialog dialog = new ModalFrameDialog( question, title, initialValue );
		dialog.show( parent );
		return dialog.retValue;
	}

	private ModalFrameDialog( String question, String title, String initialValue )
	{
		textField = new JTextField( initialValue, 20 );
		if( initialValue != null )
		{
			textField.setSelectionStart( 0 );
			textField.setSelectionEnd( initialValue.length() );
		}

		JPanel buttonPanel = new JPanel( new FlowLayout() );
		okButton = new JButton( "OK" );
		cancelButton = new JButton( "Cancel" );
		buttonPanel.add( okButton );
		buttonPanel.add( cancelButton );

		JLabel iconLabel = new JLabel( UIManager.getIcon( "OptionPane.questionIcon" ) );
		iconLabel.setVerticalAlignment( SwingConstants.TOP );
		iconLabel.setBorder( new EmptyBorder( 0, 0, 0, 10 ) );

		JPanel outerPanel = new JPanel( new BorderLayout() );
		JPanel innerPanel = new JPanel( new BorderLayout() );
		outerPanel.add( iconLabel, BorderLayout.WEST );
		outerPanel.add( innerPanel, BorderLayout.CENTER );
		outerPanel.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );

		innerPanel.add( new JLabel( question ), BorderLayout.NORTH );
		innerPanel.add( textField, BorderLayout.CENTER );
		innerPanel.add( buttonPanel, BorderLayout.SOUTH );

		frame = new JFrame( title );
		frame.getContentPane().add( outerPanel );
		frame.getRootPane().setDefaultButton( okButton );
		frame.pack();

		okButton.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				close( textField.getText() );
			}
		} );

		cancelButton.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				close( null );
			}
		} );
	}

	private void show( Frame parent )
	{
		UISupport.centerDialog( frame, parent );
		ModalFrameUtil.showAsModal( frame, parent );
	}

	private void close( String retValue )
	{
		this.retValue = retValue;
		frame.setVisible( false );
		frame.dispose();
	}
}
