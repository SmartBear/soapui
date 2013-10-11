/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com
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

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.request.AddRestRequestToTestCaseAction;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.support.AbstractModelItem;
import com.eviware.soapui.model.support.TestPropertyListenerAdapter;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JXToolBar;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static com.eviware.soapui.impl.rest.RestRequestInterface.RequestMethod;
import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation;
import static com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;

public abstract class AbstractRestRequestDesktopPanel<T extends ModelItem, T2 extends RestRequestInterface> extends
		AbstractHttpXmlRequestDesktopPanel<T, T2>
{
	protected static final int STANDARD_TOOLBAR_HEIGHT = 45;

	private InternalTestPropertyListener testPropertyListener = new InternalTestPropertyListener();
	private RestParamPropertyChangeListener restParamPropertyChangeListener = new RestParamPropertyChangeListener();

	public AbstractRestRequestDesktopPanel( T modelItem, T2 requestItem )
	{
		super( modelItem, requestItem );

		addPropertyChangeListenerToResource( requestItem );

		requestItem.addTestPropertyListener( testPropertyListener );
		requestItem.getOperation().getInterface().addPropertyChangeListener( new EndpointChangeListener() );

		for( TestProperty param : requestItem.getParams().getProperties().values() )
		{
			( ( RestParamProperty )param ).addPropertyChangeListener( restParamPropertyChangeListener );
		}
	}

	private void addPropertyChangeListenerToResource( T2 requestItem )
	{
		if( requestItem.getResource() != null )
		{
			requestItem.getResource().addPropertyChangeListener( this );
			requestItem.getResource().addTestPropertyListener( testPropertyListener );
		}
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		super.propertyChange( evt );
		updateUiValues();
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
			JButton addToTestCaseButton = createActionButton( SwingActionDelegate.createDelegate(
					AddRestRequestToTestCaseAction.SOAPUI_ACTION_ID, getRequest(), null, "/addToTestCase.gif" ), true );
			toolbar.add( addToTestCaseButton );
		}
	}

	@Override
	protected JComponent buildToolbar()
	{
		if( getRequest().getResource() != null )
		{
			JPanel panel = new JPanel( new BorderLayout() );

			JXToolBar topToolBar = UISupport.createToolbar();

			JComponent submitButton = super.getSubmitButton();
			topToolBar.add( submitButton );
			topToolBar.add( cancelButton );

			// insertButtons injects different buttons for different editors. It is overridden in other subclasses
			insertButtons( topToolBar );
			JPanel methodPanel = new JPanel( new BorderLayout() );
			JComboBox<RequestMethod> methodComboBox = new JComboBox<RequestMethod>( new RestRequestMethodModel( getRequest() ) );
			methodComboBox.setSelectedItem( getRequest().getMethod() );

			JLabel methodLabel = new JLabel( "Method" );
			methodPanel.add( methodLabel, BorderLayout.NORTH );
			methodPanel.add( methodComboBox, BorderLayout.SOUTH );
			methodPanel.setMinimumSize( new Dimension( 75, STANDARD_TOOLBAR_HEIGHT ) );
			//TODO: remove hard coded height adjustment
			methodPanel.setMaximumSize( new Dimension( 75, STANDARD_TOOLBAR_HEIGHT + 10 ) );

			JPanel endpointPanel = new JPanel( new BorderLayout() );
			endpointPanel.setMinimumSize( new Dimension( 75, STANDARD_TOOLBAR_HEIGHT ) );

			JComponent endpointCombo = buildEndpointComponent();
			super.setEndpointComponent( endpointCombo );

			JLabel endPointLabel = new JLabel( "Endpoint" );

			endpointPanel.add( endPointLabel, BorderLayout.NORTH );
			endpointPanel.add( endpointCombo, BorderLayout.SOUTH );


			topToolBar.addWithOnlyMinimumHeight( methodPanel );
			topToolBar.add( Box.createHorizontalStrut( 4 ) );
			topToolBar.addWithOnlyMinimumHeight( endpointPanel );
			topToolBar.add( Box.createHorizontalStrut( 4 ) );

			//Hook for subclasses
			addTopToolbarComponents( topToolBar );

			topToolBar.add( Box.createHorizontalGlue() );
			topToolBar.add( tabsButton );
			topToolBar.add( splitButton );
			topToolBar.add( UISupport.createToolbarButton( new ShowOnlineHelpAction( getHelpUrl() ) ) );
			int maximumPreferredHeight = findMaximumPreferredHeight( topToolBar ) + 6;
			topToolBar.setPreferredSize( new Dimension( 600, Math.max( maximumPreferredHeight, STANDARD_TOOLBAR_HEIGHT ) ) );

			panel.add( topToolBar, BorderLayout.NORTH );

			//Hook for subclasses
			addBottomToolbar( panel );

			return panel;
		}
		else
		{
			//TODO: If we don't need special clause for empty resources then remove it
			return super.buildToolbar();
		}
	}

	protected int findMaximumPreferredHeight( Container parent )
	{
		int maximum = 0;
		for( Component component : parent.getComponents() )
		{
			int componentPreferredHeight = component == null || component.getPreferredSize() == null ? 0 : component.getPreferredSize().height;
			maximum = Math.max( maximum, componentPreferredHeight );
		}

		return maximum;
	}


	//Hooks for subclasses
	protected abstract void addTopToolbarComponents( JXToolBar toolBar );
	protected abstract void addBottomToolbar( JPanel panel );


	protected abstract void updateUiValues();

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
			updateUiValues();
		}

		@Override
		public void propertyAdded( String name )
		{
			updateUiValues();
			RestParamProperty property = getRequest().getParams().getProperty( name );
			property.addPropertyChangeListener( restParamPropertyChangeListener );
		}

		@Override
		public void propertyRemoved( String name )
		{
			updateUiValues();
		}

		@Override
		public void propertyRenamed( String oldName, String newName )
		{
			updateUiValues();
		}
	}


	private class RestParamPropertyChangeListener implements PropertyChangeListener
	{
		public void propertyChange( PropertyChangeEvent evt )
		{
			try
			{
				if( evt.getPropertyName().equals( XmlBeansRestParamsTestPropertyHolder.PROPERTY_STYLE ) )
				{
					RestParamProperty source = ( RestParamProperty )evt.getSource();
					( ( AbstractModelItem )source.getModelItem() ).notifyPropertyChanged( evt.getPropertyName(),
							evt.getOldValue(), evt.getNewValue() );
				}

				if( evt.getPropertyName().equals( XmlBeansRestParamsTestPropertyHolder.PARAM_LOCATION ) )
				{
					RestParamProperty source = ( RestParamProperty )evt.getSource();
					String propName = source.getName();
					String propValue = source.getValue();
					ParameterStyle propStyle = source.getStyle();
					String requestLevelValue = getRequest().getParams().getProperty( propName ).getValue();
					removePropertyFromLevel( source.getName(), ( ParamLocation )evt.getOldValue() );
					addPropertyToLevel( propName, propValue, propStyle, ( ParamLocation )evt.getNewValue(), requestLevelValue );
				}
			}
			catch( XmlValueDisconnectedException exception )
			{
				//Do nothing, it must have been removed by another request editor instance under the same resource/method
			}
			updateUiValues();
		}


	}

	private void addPropertyToLevel( String name, String value, ParameterStyle style, ParamLocation location,
												String requestLevelValue )
	{
		RestParamsPropertyHolder paramsPropertyHolder = null;
		switch( location )
		{
			case METHOD:
				paramsPropertyHolder = getRequest().getRestMethod().getParams();
				break;
			case RESOURCE:
				paramsPropertyHolder = getRequest().getResource().getParams();
				break;
			//case REQUEST:     TODO: uncomment when we support request level parameters
			//	paramsPropertyHolder = getRequest().getParams();
			//	break;
		}

		if( paramsPropertyHolder != null )
		{
			paramsPropertyHolder.addProperty( name );
			RestParamProperty addedParameter = paramsPropertyHolder.getProperty( name );
			addedParameter.addPropertyChangeListener( restParamPropertyChangeListener );
			addedParameter.setValue( value );
			addedParameter.setDefaultValue( value );
			addedParameter.setStyle( style );
			//Override the request level value as well
			getRequest().getParams().getProperty( name ).setValue( requestLevelValue );
		}
		addPropertyChangeListenerToResource( getRequest() );
	}

	private void removePropertyFromLevel( String propertytName, ParamLocation location )
	{
		switch( location )
		{
			case METHOD:
				getRequest().getRestMethod().removeProperty( propertytName );
				break;
			case RESOURCE:
				getRequest().getResource().removeProperty( propertytName );
				break;
		}

	}


	private class EndpointChangeListener implements PropertyChangeListener
	{


		@Override
		public void propertyChange( PropertyChangeEvent evt )
		{
			if( evt.getPropertyName().equals( Interface.ENDPOINT_PROPERTY ) )
			{
				Object currentEndpoint = endpointsModel.getSelectedItem();
				if( currentEndpoint != null && currentEndpoint.equals( evt.getOldValue() ) )
				{
					endpointsModel.setSelectedItem( evt.getNewValue() );
				}
			}
		}
	}

}
