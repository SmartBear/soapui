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

package com.eviware.soapui.support.actions;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.eviware.soapui.support.UISupport;
import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * Find-and-Replace dialog for a JXmlTextArea
 * 
 * @author Ole.Matzura
 */

public class FindAndReplaceDialog extends AbstractAction
{
	private final FindAndReplaceable target;
	private JDialog dialog;
	private JCheckBox caseCheck;
	private JRadioButton allButton;
	private JRadioButton selectedLinesButton;
	private JRadioButton forwardButton;
	private JRadioButton backwardButton;
	private JCheckBox wholeWordCheck;
	private JButton findButton;
	private JButton replaceButton;
	private JButton replaceAllButton;
	private JComboBox findCombo;
	private JComboBox replaceCombo;
	private JCheckBox wrapCheck;

	public FindAndReplaceDialog( FindAndReplaceable target )
	{
		super( "Find / Replace" );
		putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "F3" ) );
		this.target = target;
	}

	public void actionPerformed( ActionEvent e )
	{
		show();
	}

	public void show()
	{
		if( dialog == null )
			buildDialog();

		replaceCombo.setEnabled( target.isEditable() );
		replaceAllButton.setEnabled( target.isEditable() );
		replaceButton.setEnabled( target.isEditable() );

		UISupport.showDialog( dialog );
		findCombo.getEditor().selectAll();
		findCombo.requestFocus();
	}

	private void buildDialog()
	{
		dialog = new JDialog( UISupport.getMainFrame(), "Find / Replace", false );

		JPanel panel = new JPanel( new BorderLayout() );
		findCombo = new JComboBox();
		findCombo.setEditable( true );
		replaceCombo = new JComboBox();
		replaceCombo.setEditable( true );

		// create inputs
		GridLayout gridLayout = new GridLayout( 2, 2 );
		gridLayout.setVgap( 5 );
		JPanel inputPanel = new JPanel( gridLayout );
		inputPanel.add( new JLabel( "Find:" ) );
		inputPanel.add( findCombo );
		inputPanel.add( new JLabel( "Replace with:" ) );
		inputPanel.add( replaceCombo );
		inputPanel.setBorder( BorderFactory.createEmptyBorder( 8, 8, 8, 8 ) );

		// create direction panel
		ButtonGroup directionGroup = new ButtonGroup();
		forwardButton = new JRadioButton( "Forward", true );
		forwardButton.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		directionGroup.add( forwardButton );
		backwardButton = new JRadioButton( "Backward" );
		backwardButton.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		directionGroup.add( backwardButton );

		JPanel directionPanel = new JPanel( new GridLayout( 2, 1 ) );
		directionPanel.add( forwardButton );
		directionPanel.add( backwardButton );
		directionPanel.setBorder( BorderFactory.createTitledBorder( "Direction" ) );

		// create scope panel
		ButtonGroup scopeGroup = new ButtonGroup();
		allButton = new JRadioButton( "All", true );
		allButton.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		selectedLinesButton = new JRadioButton( "Selected Lines" );
		selectedLinesButton.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		scopeGroup.add( allButton );
		scopeGroup.add( selectedLinesButton );

		JPanel scopePanel = new JPanel( new GridLayout( 2, 1 ) );
		scopePanel.add( allButton );
		scopePanel.add( selectedLinesButton );
		scopePanel.setBorder( BorderFactory.createTitledBorder( "Scope" ) );

		// create options
		caseCheck = new JCheckBox( "Case Sensitive" );
		caseCheck.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		wholeWordCheck = new JCheckBox( "Whole Word" );
		wholeWordCheck.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		wrapCheck = new JCheckBox( "Wrap Search" );
		wrapCheck.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		JPanel optionsPanel = new JPanel( new GridLayout( 3, 1 ) );
		optionsPanel.add( caseCheck );
		optionsPanel.add( wholeWordCheck );
		optionsPanel.add( wrapCheck );
		optionsPanel.setBorder( BorderFactory.createTitledBorder( "Options" ) );

		// create panel with options
		JPanel options = new JPanel( new GridLayout( 1, 2 ) );

		JPanel radios = new JPanel( new GridLayout( 2, 1 ) );
		radios.add( directionPanel );
		radios.add( scopePanel );

		options.add( optionsPanel );
		options.add( radios );
		options.setBorder( BorderFactory.createEmptyBorder( 0, 8, 0, 8 ) );

		// create buttons
		ButtonBarBuilder builder = new ButtonBarBuilder();
		findButton = new JButton( new FindAction() );
		builder.addFixed( findButton );
		builder.addRelatedGap();
		replaceButton = new JButton( new ReplaceAction() );
		builder.addFixed( replaceButton );
		builder.addRelatedGap();
		replaceAllButton = new JButton( new ReplaceAllAction() );
		builder.addFixed( replaceAllButton );
		builder.addUnrelatedGap();
		builder.addFixed( new JButton( new CloseAction() ) );
		builder.setBorder( BorderFactory.createEmptyBorder( 8, 8, 8, 8 ) );

		// tie it up!
		panel.add( inputPanel, BorderLayout.NORTH );
		panel.add( options, BorderLayout.CENTER );
		panel.add( builder.getPanel(), BorderLayout.SOUTH );

		dialog.getContentPane().add( panel );
		dialog.pack();
		UISupport.initDialogActions( dialog, null, findButton );
	}

	private int findNext( int pos, String txt, String value )
	{
		int ix = forwardButton.isSelected() ? txt.indexOf( value, pos ) : txt.lastIndexOf( value, pos );

		if( wholeWordCheck.isSelected() )
		{
			while( ix != -1
					&& ( ix > 0 && Character.isLetterOrDigit( txt.charAt( ix - 1 ) ) )
					|| ( ix < txt.length() - value.length() - 1 && Character.isLetterOrDigit( txt.charAt( ix
							+ value.length() ) ) ) )
			{
				ix = findNext( ix, txt, value );
			}
		}

		if( ix == -1 && wrapCheck.isSelected() )
		{
			if( forwardButton.isSelected() && pos > 0 )
				return findNext( 0, txt, value );
			else if( backwardButton.isSelected() && pos < txt.length() - 1 )
				return findNext( txt.length() - 1, txt, value );
		}

		if( selectedLinesButton.isSelected() && ( ix < target.getSelectionStart() || ix > target.getSelectionEnd() ) )
			ix = -1;

		return ix;
	}

	private class FindAction extends AbstractAction
	{
		public FindAction()
		{
			super( "Find" );
		}

		public void actionPerformed( ActionEvent e )
		{
			int pos = target.getCaretPosition();
			int selstart = target.getSelectionStart();
			if( selstart < pos && selstart != -1 )
				pos = selstart;

			String txt = target.getText();

			if( findCombo.getSelectedItem() == null )
			{
				return;
			}
			String value = findCombo.getSelectedItem().toString();
			if( value.length() == 0 || pos == txt.length() )
				return;

			if( !caseCheck.isSelected() )
			{
				value = value.toUpperCase();
				txt = txt.toUpperCase();
			}

			if( txt.substring( pos, pos + value.length() ).equals( value ) )
			{
				pos += forwardButton.isSelected() ? 1 : -1;
			}

			int ix = findNext( pos, txt, value );

			if( ix != -1 )
			{
				target.select( ix, ix + value.length() );

				for( int c = 0; c < findCombo.getItemCount(); c++ )
				{
					if( findCombo.getItemAt( c ).equals( value ) )
					{
						findCombo.removeItem( c );
						break;
					}
				}

				findCombo.insertItemAt( value, 0 );
			}
			else
				Toolkit.getDefaultToolkit().beep();
		}
	}

	private class ReplaceAction extends AbstractAction
	{
		public ReplaceAction()
		{
			super( "Replace" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( target.getSelectedText() == null )
				return;

			String value = replaceCombo.getSelectedItem().toString();
			int ix = target.getSelectionStart();
			target.setSelectedText( value );
			target.select( ix + value.length(), ix );

			for( int c = 0; c < replaceCombo.getItemCount(); c++ )
			{
				if( replaceCombo.getItemAt( c ).equals( value ) )
				{
					replaceCombo.removeItem( c );
					break;
				}
			}

			replaceCombo.insertItemAt( value, 0 );
		}
	}

	private class ReplaceAllAction extends AbstractAction
	{
		public ReplaceAllAction()
		{
			super( "Replace All" );
		}

		public void actionPerformed( ActionEvent e )
		{
			int pos = target.getCaretPosition();
			String txt = target.getText();

			if( findCombo.getSelectedItem() == null )
			{
				return;
			}
			String value = findCombo.getSelectedItem().toString();
			if( value.length() == 0 || txt.length() == 0 )
				return;
			String newValue = replaceCombo.getSelectedItem().toString();

			if( !caseCheck.isSelected() )
			{
				if( newValue.equalsIgnoreCase( value ) )
					return;
				value = value.toUpperCase();
				txt = txt.toUpperCase();
			}
			else if( newValue.equals( value ) )
				return;

			int ix = findNext( pos, txt, value );
			int firstIx = ix;
			int valueInNewValueIx = !caseCheck.isSelected() ? newValue.toUpperCase().indexOf( value ) : newValue
					.indexOf( value );

			while( ix != -1 )
			{
				System.out.println( "found match at " + ix + ", " + firstIx + ", " + valueInNewValueIx );
				target.select( ix + value.length(), ix );

				target.setSelectedText( newValue );
				target.select( ix + newValue.length(), ix );

				// adjust firstix
				if( ix < firstIx )
					firstIx += newValue.length() - value.length();

				txt = target.getText();
				if( !caseCheck.isSelected() )
				{
					txt = txt.toUpperCase();
				}

				if( forwardButton.isSelected() )
				{
					ix = findNext( ix + newValue.length(), txt, value );
				}
				else
				{
					ix = findNext( ix - 1, txt, value );
				}
				if( wrapCheck.isSelected() && valueInNewValueIx != -1 && ix == firstIx + valueInNewValueIx )
				{
					break;
				}
			}
		}
	}

	private class CloseAction extends AbstractAction
	{
		public CloseAction()
		{
			super( "Close" );
		}

		public void actionPerformed( ActionEvent e )
		{
			dialog.setVisible( false );
		}
	}

}
