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

package com.eviware.soapui.impl.rest.panels.request;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.text.Document;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestInterface;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.support.TestPropertyListenerAdapter;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.propertyexpansion.PropertyExpansionPopupListener;

public abstract class AbstractRestRequestDesktopPanel<T extends ModelItem, T2 extends RestRequestInterface> extends
		AbstractHttpXmlRequestDesktopPanel<T, T2>
{
	private boolean updatingRequest;
	private JUndoableTextField pathTextField;
	private JComboBox acceptCombo;
	private JLabel pathLabel;
	private boolean updating;
	private InternalTestPropertyListener testPropertyListener = new InternalTestPropertyListener();
	private RestParamPropertyChangeListener restParamPropertyChangeListener = new RestParamPropertyChangeListener();
	private JComboBox pathCombo;

	public AbstractRestRequestDesktopPanel( T modelItem, T2 requestItem )
	{
		super( modelItem, requestItem );

		if( requestItem.getResource() != null )
		{
			requestItem.getResource().addPropertyChangeListener( this );
		}

		requestItem.addTestPropertyListener( testPropertyListener );

		for( TestProperty param : requestItem.getParams().getProperties().values() )
		{
			( ( RestParamProperty )param ).addPropertyChangeListener( restParamPropertyChangeListener );
		}
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		updateFullPathLabel();

		if( evt.getPropertyName().equals( "accept" ) && !updatingRequest )
		{
			acceptCombo.setSelectedItem( evt.getNewValue() );
		}
		else if( evt.getPropertyName().equals( "responseMediaTypes" ) && !updatingRequest )
		{
			Object item = acceptCombo.getSelectedItem();
			acceptCombo.setModel( new DefaultComboBoxModel( ( Object[] )evt.getNewValue() ) );
			acceptCombo.setSelectedItem( item );
		}
		else if( (evt.getPropertyName().equals( "path" ) || evt.getPropertyName().equals( "restMethod" ))
				&& ( getRequest().getResource() == null || getRequest().getResource() == evt.getSource() ) )
		{
			if( pathLabel != null )
			{
				updateFullPathLabel();
			}

			if( !updating && pathTextField != null )
			{
				updating = true;
				pathTextField.setText( ( String )evt.getNewValue() );
				pathTextField.setToolTipText( pathTextField.getText() );
				updating = false;
			}
		}

		super.propertyChange( evt );
	}

	@Override
	protected Submit doSubmit() throws SubmitException
	{
		return getRequest().submit( new WsdlSubmitContext( getModelItem() ), true );
	}

	@Override
	protected String getHelpUrl()
	{
		return null;
	}

	@Override
	protected void insertButtons( JXToolBar toolbar )
	{
		if( getRequest().getResource() == null )
		{
			addToolbarComponents( toolbar );
		}
	}

	@Override
	protected JComponent buildToolbar()
	{
		if( getRequest().getResource() != null )
		{
			JPanel panel = new JPanel( new BorderLayout() );
			panel.add( super.buildToolbar(), BorderLayout.NORTH );

			JXToolBar toolbar = UISupport.createToolbar();
			addToolbarComponents( toolbar );

			panel.add( toolbar, BorderLayout.SOUTH );
			return panel;
		}
		else
		{
			return super.buildToolbar();
		}
	}

	protected void addToolbarComponents( JXToolBar toolbar )
	{
		toolbar.addSeparator();

		if( getRequest().getResource() != null )
		{
			acceptCombo = new JComboBox( getRequest().getResponseMediaTypes() );
			acceptCombo.setEditable( true );
			acceptCombo.setToolTipText( "Sets accepted encoding(s) for response" );
			acceptCombo.setSelectedItem( getRequest().getAccept() );
			acceptCombo.addItemListener( new ItemListener()
			{
				public void itemStateChanged( ItemEvent e )
				{
					updatingRequest = true;
					getRequest().setAccept( String.valueOf( acceptCombo.getSelectedItem() ) );
					updatingRequest = false;
				}
			} );

			toolbar.addLabeledFixed( "Accept", acceptCombo );
			toolbar.addSeparator();

			if( getRequest() instanceof RestTestRequestInterface )
			{
				pathCombo = new JComboBox( new PathComboBoxModel() );
				pathCombo.setRenderer( new RestMethodListCellRenderer() );
				pathCombo.setPreferredSize( new Dimension( 200, 20 ) );
				pathCombo.setSelectedItem( getRequest().getRestMethod() );

				toolbar.addLabeledFixed( "Resource/Method:", pathCombo );
				toolbar.addSeparator();
			}
			else
			{
				toolbar.add( new JLabel( "Full Path: " ) );
			}

			pathLabel = new JLabel();
			updateFullPathLabel();

			toolbar.add( pathLabel );
		}
		else
		{
			pathTextField = new JUndoableTextField();
			pathTextField.setPreferredSize( new Dimension( 300, 20 ) );
			pathTextField.setText( getRequest().getPath() );
			pathTextField.setToolTipText( pathTextField.getText() );
			pathTextField.getDocument().addDocumentListener( new DocumentListenerAdapter()
			{
				@Override
				public void update( Document document )
				{
					if( updating )
						return;

					updating = true;
					getRequest().setPath( pathTextField.getText() );
					updating = false;
				}
			} );
			PropertyExpansionPopupListener.enable( pathTextField, getModelItem() );

			toolbar.addLabeledFixed( "Request URL:", pathTextField );
		}

		toolbar.addSeparator();
	}

	protected boolean release()
	{
		if( getRequest().getResource() != null )
		{
			getRequest().getResource().removePropertyChangeListener( this );
		}

		getRequest().removeTestPropertyListener( testPropertyListener );

		for( TestProperty param : getRequest().getParams().getProperties().values() )
		{
			( ( RestParamProperty )param ).removePropertyChangeListener( restParamPropertyChangeListener );
		}

		return super.release();
	}

	private class InternalTestPropertyListener extends TestPropertyListenerAdapter
	{
		@Override
		public void propertyValueChanged( String name, String oldValue, String newValue )
		{
			updateFullPathLabel();
		}

		@Override
		public void propertyAdded( String name )
		{
			updateFullPathLabel();

			getRequest().getParams().getProperty( name ).addPropertyChangeListener( restParamPropertyChangeListener );
		}

		@Override
		public void propertyRemoved( String name )
		{
			updateFullPathLabel();
		}

		@Override
		public void propertyRenamed( String oldName, String newName )
		{
			updateFullPathLabel();
		}
	}

	private void updateFullPathLabel()
	{
		if( pathLabel != null && getRequest().getResource() != null )
		{
			String text = RestUtils.expandPath( getRequest().getResource().getFullPath(), getRequest().getParams(),
					getRequest() );
			pathLabel.setText( "[" + text + "]" );
			pathLabel.setToolTipText( text );
		}
	}

	private class RestParamPropertyChangeListener implements PropertyChangeListener
	{
		public void propertyChange( PropertyChangeEvent evt )
		{
			updateFullPathLabel();
		}
	}

	private class PathComboBoxModel extends AbstractListModel implements ComboBoxModel
	{
		public int getSize()
		{
			int sz = 0; 
			for( RestResource resource : getRequest().getResource().getService().getAllResources())
			{
				sz += resource.getRestMethodCount();
			}
			
			return sz;
		}

		public Object getElementAt( int index )
		{
			int sz = 0; 
			for( RestResource resource : getRequest().getResource().getService().getAllResources())
			{
				if( index < sz + resource.getRestMethodCount() )
				{
					return resource.getRestMethodAt( index-sz );
				}
				
				sz += resource.getRestMethodCount();
			}
			
			return null;
		}

		public void setSelectedItem( Object anItem )
		{
			( ( RestTestRequestInterface )getRequest() ).getTestStep().setRestMethod( (RestMethod)anItem );
		}

		public Object getSelectedItem()
		{
			return getRequest().getRestMethod();
		}
	}

	private class RestMethodListCellRenderer extends DefaultListCellRenderer
	{
		@Override
		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus )
		{
			Component result = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

			if( value instanceof RestMethod )
			{
				RestMethod item = ( RestMethod )value;
				setIcon( item.getIcon() );
				setText( item.getResource().getName() + " -> " + item.getName() );
			}

			return result;
		}

	}

}
