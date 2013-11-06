/*
 *  SoapUI, copyright (C) 2004-2013 smartbear.com 
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.rest.panels.component;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.jgoodies.forms.factories.ButtonBarFactory;
import org.apache.commons.lang.mutable.MutableBoolean;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Text field for editing a rest resource. Pops up a separate dialog to edit parts of the resource separately if the
 * rest resource has parents or children.
 */
public class RestResourceEditor extends JTextField
{
	private RestResource editingRestResource;
	private MutableBoolean updating;
	private List<RestSubResourceTextField> restSubResourceTextFields;
	private int lastSelectedPosition;

	public RestResourceEditor( final RestResource editingRestResource, MutableBoolean updating )
	{
		super( editingRestResource.getFullPath() );
		this.editingRestResource = editingRestResource;
		this.updating = updating;

		if( isResourceLonely( editingRestResource ) )
		{
			getDocument().addDocumentListener( new LonelyDocumentListener() );
			addFocusListener( new FocusListener()
			{
				public void focusLost( FocusEvent e )
				{
					scanForTemplateParameters();
				}

				public void focusGained( FocusEvent e )
				{
				}
			} );

		}
		else
		{
			Color originalBackground = getBackground();
			Border originalBorder = getBorder();
			setEditable( false );
			setBackground( originalBackground );
			setBorder( originalBorder );
			setCursor( Cursor.getPredefinedCursor( Cursor.TEXT_CURSOR ) );

			addMouseListener( new MouseAdapter()
			{
				@Override
				public void mouseClicked( MouseEvent e )
				{
					final RestResource focusedResource = new RestResourceFinder( editingRestResource ).findResourceAt( lastSelectedPosition );
					SwingUtilities.invokeLater( new Runnable()
					{
						@Override
						public void run()
						{
							openPopup( focusedResource );
						}
					} );
				}
			} );
			addCaretListener( new CaretListener()
			{
				@Override
				public void caretUpdate( final CaretEvent e )
				{
					lastSelectedPosition = e.getDot();
				}

			} );
		}
	}

	void scanForTemplateParameters()
	{
		for( RestResource restResource : RestUtils.extractAncestorsParentFirst( editingRestResource ) )
		{
			for( String p : RestUtils.extractTemplateParams( restResource.getPath() ) )
			{
				if( !resourceOrParentHasProperty( restResource, p ) )
				{
					RestParamProperty property = restResource.addProperty( p );
					property.setStyle( RestParamsPropertyHolder.ParameterStyle.TEMPLATE );
					String value = UISupport.prompt( "Specify default value for parameter [" + p + "]",
							"Add Parameter", "" );
					if( value != null )
					{
						property.setDefaultValue( value );
						property.setValue( value );
					}
				}
			}
		}
	}

	private boolean resourceOrParentHasProperty( RestResource restResource, String name )
	{
		for( RestResource r = restResource; r != null; r = r.getParentResource() )
		{
			if( r.hasProperty( name ) )
			{
				return true;
			}
		}
		return false;
	}

	private boolean isResourceLonely( RestResource restResource )
	{
		return restResource.getParentResource() == null;
	}

	public void openPopup( RestResource focusedResource )
	{
		final JPanel panel = createResourceEditorPanel( focusedResource );

		PopupWindow popupWindow = new PopupWindow( panel );
		moveWindowBelowTextField( popupWindow );
		popupWindow.setVisible( true );
	}

	private JPanel createResourceEditorPanel( RestResource focusedResource )
	{
		final JPanel panel = new JPanel( new BorderLayout() );

		Box contentBox = Box.createVerticalBox();

		int index = 0;

		ImageIcon icon = UISupport.createImageIcon( "/connector.png" );

		final JLabel changeWarningLabel = new JLabel( " " );
		changeWarningLabel.setBorder( BorderFactory.createCompoundBorder(
				contentBox.getBorder(),
				BorderFactory.createEmptyBorder( 10, 0, 0, 0 ) ) );
		restSubResourceTextFields = new ArrayList<RestSubResourceTextField>();
		DocumentListener pathChangedListener = new DocumentListenerAdapter()
		{
			@Override
			public void update( Document document )
			{
				int affectedRequestCount = 0;
				for( RestSubResourceTextField restResourceTextField : restSubResourceTextFields )
				{
					if( !restResourceTextField.getTextField().getText().equals( restResourceTextField.getRestResource().getPath() ) )
					{
						affectedRequestCount = restResourceTextField.getAffectedRequestCount();
						break;
					}
				}
				if( affectedRequestCount > 0 )
				{
					changeWarningLabel.setText( String.format( "<html>Changes will affect <b>%d</b> request%s</html>",
							affectedRequestCount, affectedRequestCount > 1 ? "s" : "" ) );
					changeWarningLabel.setVisible( true );
				}
				else
				{
					changeWarningLabel.setVisible( false );
				}
			}
		};
		for( RestResource restResource : RestUtils.extractAncestorsParentFirst( editingRestResource ) )
		{
			Box row = Box.createHorizontalBox();
			row.setAlignmentX( 0 );

			if( index > 1 )
			{
				row.add( Box.createHorizontalStrut( ( index - 1 ) * icon.getIconWidth() ) );
			}
			if( index >= 1 )
			{
				row.add( new JLabel( icon ) );
			}

			final RestSubResourceTextField restSubResourceTextField = new RestSubResourceTextField( restResource );
			restSubResourceTextField.getTextField().getDocument().addDocumentListener( pathChangedListener );
			restSubResourceTextFields.add( restSubResourceTextField );

			Box textFieldBox = Box.createVerticalBox();
			textFieldBox.add( Box.createVerticalGlue() );
			textFieldBox.add( restSubResourceTextField.getTextField() );
			row.add( textFieldBox );

			contentBox.add( row );
			if( restResource == focusedResource )
			{
				SwingUtilities.invokeLater( new Runnable()
				{
					@Override
					public void run()
					{
						restSubResourceTextField.getTextField().requestFocusInWindow();
						restSubResourceTextField.getTextField().selectAll();
					}
				} );
			}

			index++;
		}

		panel.add( contentBox, BorderLayout.NORTH );

		panel.add( changeWarningLabel, BorderLayout.CENTER );

		panel.setBorder( BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder( 0, 0, 1, 0, Color.BLACK ),
				BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) ) );

		return panel;
	}

	private class RestSubResourceTextField
	{
		private RestResource restResource;
		private JTextField textField;
		private Integer affectedRequestCount;

		private RestSubResourceTextField( RestResource restResource )
		{
			this.restResource = restResource;
			textField = new JTextField( restResource.getPath() );
			textField.setMaximumSize( new Dimension( 340, ( int )textField.getPreferredSize().getHeight() ) );
			textField.setPreferredSize( new Dimension( 340, ( int )textField.getPreferredSize().getHeight() ) );
		}

		public JTextField getTextField()
		{
			return textField;
		}

		public RestResource getRestResource()
		{
			return restResource;
		}

		public int getAffectedRequestCount()
		{
			if( affectedRequestCount == null )
			{
				int count = restResource.getRequestCount();
				for( RestResource childResource : restResource.getAllChildResources() )
				{
					count += childResource.getRequestCount();
				}
				affectedRequestCount = count;
			}
			return affectedRequestCount;
		}
	}

	private class LonelyDocumentListener extends DocumentListenerAdapter
	{
		@Override
		public void update( Document document )
		{
			if( updating.booleanValue() )
			{
				return;
			}
			updating.setValue( true );
			editingRestResource.setPath( getText( document ).trim() );
			updating.setValue( false );
		}
	}

	private class PopupWindow extends JDialog
	{

		private PopupWindow( final JPanel panel )
		{
			super( SoapUI.getFrame() );
			setModal( true );
			setResizable( false );
			setMinimumSize( new Dimension( 230, 0 ) );

			JPanel contentPane = new JPanel( new BorderLayout() );
			setContentPane( contentPane );


			JButton okButton = new JButton( new AbstractAction( "OK" )
			{
				@Override
				public void actionPerformed( ActionEvent e )
				{
					for( RestSubResourceTextField restSubResourceTextField : restSubResourceTextFields )
					{
						restSubResourceTextField.getRestResource().setPath( restSubResourceTextField.getTextField().getText().trim() );
					}
					scanForTemplateParameters();
					dispose();
				}
			} );

			AbstractAction cancelAction = new AbstractAction( "Cancel" )
			{
				@Override
				public void actionPerformed( ActionEvent e )
				{
					dispose();
				}
			};
			JButton cancelButton = new JButton( cancelAction );
			cancelButton.getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), "cancel" );
			cancelButton.getActionMap().put( "cancel", cancelAction );

			JPanel buttonBar = ButtonBarFactory.buildRightAlignedBar( okButton, cancelButton );
			buttonBar.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
			contentPane.add( panel, BorderLayout.CENTER );
			contentPane.add( buttonBar, BorderLayout.SOUTH );
			getRootPane().setDefaultButton( okButton );

			pack();
		}
	}

	private void moveWindowBelowTextField( PopupWindow popupWindow )
	{
		try
		{
			Point textFieldLocation = this.getLocationOnScreen();
			popupWindow.setLocation( textFieldLocation.x, textFieldLocation.y + this.getHeight() );
		}
		catch( IllegalComponentStateException ignore )
		{
			// this will happen when the desktop panel is being closed
		}
	}

}
