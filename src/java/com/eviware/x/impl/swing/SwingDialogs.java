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

package com.eviware.x.impl.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;

import com.eviware.soapui.support.DefaultHyperlinkListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.ProgressDialog;
import com.eviware.x.dialogs.XDialogs;
import com.eviware.x.dialogs.XProgressDialog;
import com.jgoodies.forms.factories.ButtonBarFactory;

/**
 * 
 * @author Lars
 */
public class SwingDialogs implements XDialogs
{
	private Component parent;
	private JDialog extendedInfoDialog;
	private Boolean extendedInfoResult;

	public SwingDialogs( Component parent )
	{
		this.parent = parent;
	}

	public void showErrorMessage( String message )
	{
		JOptionPane.showMessageDialog( parent, message, "Error", JOptionPane.ERROR_MESSAGE );
	}

	public boolean confirm( String question, String title )
	{
		return JOptionPane.showConfirmDialog( parent, question, title, JOptionPane.YES_NO_OPTION ) == JOptionPane.OK_OPTION;
	}

	public String prompt( String question, String title, String value )
	{
		return ( String )JOptionPane.showInputDialog( parent, question, title, JOptionPane.QUESTION_MESSAGE, null, null,
				value );
	}

	public String prompt( String question, String title )
	{
		return JOptionPane.showInputDialog( parent, question, title, JOptionPane.QUESTION_MESSAGE );
	}

	public void showInfoMessage( String message )
	{
		showInfoMessage( message, "Information" );
	}

	public void showInfoMessage( String message, String title )
	{
		JOptionPane.showMessageDialog( parent, message, title, JOptionPane.INFORMATION_MESSAGE );
	}

	public Object prompt( String question, String title, Object[] objects )
	{
		Object result = JOptionPane.showInputDialog( parent, question, title, JOptionPane.OK_CANCEL_OPTION, null,
				objects, null );
		return result;
	}

	public Object prompt( String question, String title, Object[] objects, String value )
	{
		Object result = JOptionPane.showInputDialog( parent, question, title, JOptionPane.OK_CANCEL_OPTION, null,
				objects, value );
		return result;
	}

	public Boolean confirmOrCancel( String question, String title )
	{
		int result = JOptionPane.showConfirmDialog( parent, question, title, JOptionPane.YES_NO_CANCEL_OPTION );

		if( result == JOptionPane.CANCEL_OPTION )
			return null;

		return Boolean.valueOf( result == JOptionPane.YES_OPTION );
	}

	public int yesYesToAllOrNo( String question, String title )
	{
		String[] buttons = { "Yes", "Yes to all", "No" };
		return JOptionPane.showOptionDialog( parent, question, title, 0, JOptionPane.QUESTION_MESSAGE, null, buttons,
				buttons[0] );
	}

	public XProgressDialog createProgressDialog( String label, int length, String initialValue, boolean canCancel )
	{
		return new ProgressDialog( "Progress", label, length, initialValue, canCancel );
	}

	public void showExtendedInfo( String title, String description, String content, Dimension size )
	{
		JPanel buttonBar = ButtonBarFactory.buildRightAlignedBar( new JButton( new OkAction( "OK" ) ) );

		showExtendedInfo( title, description, content, buttonBar, size );
	}

	private void showExtendedInfo( String title, String description, String content, JPanel buttonBar, Dimension size )
	{
		extendedInfoDialog = new JDialog( UISupport.getMainFrame(), title );
		extendedInfoDialog.setModal( true );
		JPanel panel = new JPanel( new BorderLayout() );

		if( description != null )
		{
			panel.add( UISupport.buildDescription( title, description, null ), BorderLayout.NORTH );
		}

		JEditorPane editorPane = new JEditorPane( "text/html", content );
		editorPane.setCaretPosition( 0 );
		editorPane.setEditable( false );
		editorPane.addHyperlinkListener( new DefaultHyperlinkListener( editorPane ) );

		JScrollPane scrollPane = new JScrollPane( editorPane );
		scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ),
				scrollPane.getBorder() ) );

		panel.add( scrollPane );
		buttonBar.setBorder( BorderFactory.createEmptyBorder( 0, 0, 5, 5 ) );
		panel.add( buttonBar, BorderLayout.SOUTH );

		extendedInfoDialog.getRootPane().setContentPane( panel );
		if( size == null )
			extendedInfoDialog.setSize( 400, 300 );
		else
			extendedInfoDialog.setSize( size );

		extendedInfoResult = null;
		UISupport.showDialog( extendedInfoDialog );
	}

	public boolean confirmExtendedInfo( String title, String description, String content, Dimension size )
	{
		JPanel buttonBar = ButtonBarFactory.buildRightAlignedBar( new JButton( new OkAction( "OK" ) ), new JButton(
				new CancelAction( "Cancel" ) ) );

		showExtendedInfo( title, description, content, buttonBar, size );

		return extendedInfoResult == null ? false : extendedInfoResult;
	}

	public Boolean confirmOrCancleExtendedInfo( String title, String description, String content, Dimension size )
	{
		JPanel buttonBar = ButtonBarFactory.buildRightAlignedBar( new JButton( new OkAction( "Yes" ) ), new JButton(
				new NoAction( "No" ) ), new JButton( new CancelAction( "Cancel" ) ) );

		showExtendedInfo( title, description, content, buttonBar, size );

		return extendedInfoResult;
	}

	private final class OkAction extends AbstractAction
	{
		public OkAction( String name )
		{
			super( name );
		}

		public void actionPerformed( ActionEvent e )
		{
			extendedInfoResult = true;
			extendedInfoDialog.setVisible( false );
		}
	}

	private final class NoAction extends AbstractAction
	{
		public NoAction( String name )
		{
			super( name );
		}

		public void actionPerformed( ActionEvent e )
		{
			extendedInfoResult = false;
			extendedInfoDialog.setVisible( false );
		}
	}

	private final class CancelAction extends AbstractAction
	{
		public CancelAction( String name )
		{
			super( name );
		}

		public void actionPerformed( ActionEvent e )
		{
			extendedInfoResult = null;
			extendedInfoDialog.setVisible( false );
		}
	}

	public String selectXPath( String title, String info, String xml, String xpath )
	{
		return prompt( "Specify XPath expression", "Select XPath", xpath );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.x.dialogs.XDialogs#promptPassword(java.lang.String,
	 * java.lang.String)
	 */
	public char[] promptPassword( String question, String title )
	{
		JPasswordField passwordField = new JPasswordField();
		JLabel qLabel = new JLabel( question );
		JOptionPane.showConfirmDialog( null, new Object[] { qLabel, passwordField }, title, JOptionPane.OK_CANCEL_OPTION );
		return passwordField.getPassword();
	}
}
