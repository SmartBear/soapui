/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.mock;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.ImageIcon;

import com.eviware.soapui.config.*;
import com.eviware.soapui.impl.support.AbstractMockOperation;
import com.eviware.soapui.model.iface.InterfaceListener;
import com.eviware.soapui.model.project.ProjectListener;
import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.dispatch.MockOperationDispatchRegistry;
import com.eviware.soapui.impl.wsdl.mock.dispatch.MockOperationDispatcher;
import com.eviware.soapui.impl.wsdl.support.CompressedStringSupport;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.support.InterfaceListenerAdapter;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.UISupport;

/**
 * A WsdlMockOperation in a WsdlMockService
 * 
 * @author ole.matzura
 */

public class WsdlMockOperation extends AbstractMockOperation<MockOperationConfig, WsdlMockResponse>
{
	@SuppressWarnings( "unused" )
	private final static Logger log = Logger.getLogger( WsdlMockOperation.class );

	public final static String DISPATCH_STYLE_PROPERTY = WsdlMockOperation.class.getName() + "@dispatchstyle";
	public final static String DISPATCH_PATH_PROPERTY = WsdlMockOperation.class.getName() + "@dispatchpath";
	public final static String OPERATION_PROPERTY = WsdlMockOperation.class.getName() + "@operation";

	private WsdlOperation operation;
	private MockOperationDispatcher dispatcher;
	private InterfaceListener interfaceListener = new InternalInterfaceListener();
	private ProjectListener projectListener = new InternalProjectListener();
	private ImageIcon oneWayIcon;
	private ImageIcon notificationIcon;
	private ImageIcon solicitResponseIcon;

	public WsdlMockOperation( WsdlMockService mockService, MockOperationConfig config )
	{
		super( config, mockService, "/mockOperation.gif" );

		Interface iface = mockService.getProject().getInterfaceByName( config.getInterface() );
		if( iface == null )
		{
			SoapUI.log.warn( "Missing interface [" + config.getInterface() + "] for MockOperation in project" );
		}
		else
		{
			operation = ( WsdlOperation )iface.getOperationByName( config.getOperation() );
		}

		List<MockResponseConfig> responseConfigs = config.getResponseList();
		for( MockResponseConfig responseConfig : responseConfigs )
		{
			WsdlMockResponse wsdlMockResponse = new WsdlMockResponse( this, responseConfig );
			wsdlMockResponse.addPropertyChangeListener( this );
			super.addMockResponse( wsdlMockResponse );
		}

		setupConfig( config );
	}

	public WsdlMockOperation( WsdlMockService mockService, MockOperationConfig config, WsdlOperation operation )
	{
		super( config, mockService, "/mockOperation.gif" );
		this.operation = operation;

		config.setInterface( operation.getInterface().getName() );
		config.setOperation( operation.getName() );

		setupConfig( config );
	}

	protected void setupConfig( MockOperationConfig config )
	{
		super.setupConfig( config );

		if( !config.isSetDispatchStyle() )
			config.setDispatchStyle( MockOperationDispatchStyleConfig.SEQUENCE );

		if( !getConfig().isSetDispatchConfig() )
			getConfig().addNewDispatchConfig();

		dispatcher = MockOperationDispatchRegistry.buildDispatcher( config.getDispatchStyle().toString(), this );

		createIcons();
		addListeners();
	}

	private void addListeners()
	{
		Operation operation = getOperation();
		if( operation != null )
		{
			operation.getInterface().getProject().addProjectListener( projectListener );
			operation.getInterface().addInterfaceListener( interfaceListener );
			operation.getInterface().addPropertyChangeListener( WsdlInterface.NAME_PROPERTY, this );
		}
	}

	private void createIcons()
	{
		oneWayIcon = UISupport.createImageIcon( "/onewaymockoperation.gif" );
		notificationIcon = UISupport.createImageIcon( "/mocknotificationoperation.gif" );
		solicitResponseIcon = UISupport.createImageIcon( "/mocksolicitresponseoperation.gif" );
	}

	@Override
	public ImageIcon getIcon()
	{
		if( operation != null )
		{
			if( isOneWay() )
			{
				return oneWayIcon;
			}
			else if( isNotification() )
			{
				return notificationIcon;
			}
			else if( isSolicitResponse() )
			{
				return solicitResponseIcon;
			}
		}

		return super.getIcon();
	}

	public WsdlMockService getMockService()
	{
		return ( WsdlMockService )getParent();
	}

	public WsdlOperation getOperation()
	{
		return operation;
	}

	public WsdlMockResponse addNewMockResponse( MockResponseConfig responseConfig )
	{
		WsdlMockResponse mockResponse = new WsdlMockResponse( this, responseConfig );

		super.addMockResponse( mockResponse );
		if( getMockResponseCount() == 1 )
			setDefaultResponse( mockResponse.getName() );

		// add ws-a action
		WsdlUtils.setDefaultWsaAction( mockResponse.getWsaConfig(), true );

		( getMockService() ).fireMockResponseAdded( mockResponse );
		notifyPropertyChanged( "mockResponses", null, mockResponse );

		return mockResponse;
	}

	public WsdlMockResponse addNewMockResponse( String name, boolean createResponse )
	{
		MockResponseConfig responseConfig = (MockResponseConfig)getConfig().addNewResponse();
		responseConfig.setName( name );
		responseConfig.addNewResponseContent();

		if( createResponse && getOperation() != null && getOperation().isBidirectional() )
		{
			boolean createOptional = SoapUI.getSettings().getBoolean(
					WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS );
			CompressedStringSupport.setString( responseConfig.getResponseContent(),
					getOperation().createResponse( createOptional ) );
		}

		return addNewMockResponse( responseConfig );
	}

	public WsdlMockResult dispatchRequest( WsdlMockRequest request ) throws DispatchException
	{
		try
		{
			request.setOperation( getOperation() );
			WsdlMockResult result = new WsdlMockResult( request );

			if( getMockResponseCount() == 0 )
				throw new DispatchException( "Missing MockResponse(s) in MockOperation [" + getName() + "]" );

			result.setMockOperation( this );
			WsdlMockResponse response = ( WsdlMockResponse )dispatcher.selectMockResponse( request, result );
			if( response == null )
			{
				response = getMockResponseByName( getDefaultResponse() );
			}

			if( response == null )
			{
				throw new DispatchException( "Failed to find MockResponse" );
			}

			result.setMockResponse( response );
			response.execute( request, result );

			return result;
		}
		catch( Throwable e )
		{
			if( e instanceof DispatchException )
				throw ( DispatchException )e;
			else
				throw new DispatchException( e );
		}
	}

	@Override
	public void release()
	{
		super.release();

		if( dispatcher != null )
			dispatcher.release();

		for( MockResponse response : getMockResponses() )
		{
			response.removePropertyChangeListener( this );
			response.release();
		}

		if( operation != null )
		{
			operation.getInterface().getProject().removeProjectListener( projectListener );
			operation.getInterface().removeInterfaceListener( interfaceListener );
			operation.getInterface().removePropertyChangeListener( WsdlInterface.NAME_PROPERTY, this );
		}
	}

	public String getDispatchStyle()
	{
		return String.valueOf( getConfig().isSetDispatchStyle() ? getConfig().getDispatchStyle()
				: MockOperationDispatchStyleConfig.SEQUENCE );
	}

	public MockOperationDispatcher setDispatchStyle( String dispatchStyle )
	{
		String old = getDispatchStyle();
		if( dispatcher != null && dispatchStyle.equals( old ) )
			return dispatcher;

		getConfig().setDispatchStyle( MockOperationDispatchStyleConfig.Enum.forString( dispatchStyle ) );

		if( dispatcher != null )
		{
			dispatcher.release();
		}

		if( !getConfig().isSetDispatchConfig() )
			getConfig().addNewDispatchConfig();

		dispatcher = MockOperationDispatchRegistry.buildDispatcher( dispatchStyle, this );

		notifyPropertyChanged( DISPATCH_STYLE_PROPERTY, old, dispatchStyle );

		return dispatcher;
	}

	public String getDispatchPath()
	{
		return getConfig().getDispatchPath();
	}

	public void setDispatchPath( String dispatchPath )
	{
		String old = getDispatchPath();
		getConfig().setDispatchPath( dispatchPath );
		notifyPropertyChanged( DISPATCH_PATH_PROPERTY, old, dispatchPath );
	}

	public void propertyChange( PropertyChangeEvent arg0 )
	{
		if( arg0.getPropertyName().equals( WsdlMockResponse.NAME_PROPERTY ) )
		{
			if( arg0.getOldValue().equals( getDefaultResponse() ) )
				setDefaultResponse( arg0.getNewValue().toString() );
		}
		else if( arg0.getPropertyName().equals( WsdlInterface.NAME_PROPERTY ) )
		{
			getConfig().setInterface( arg0.getNewValue().toString() );
		}
	}

	public void setOperation( WsdlOperation operation )
	{
		WsdlOperation oldOperation = getOperation();

		if( operation == null )
		{
			getConfig().unsetInterface();
			getConfig().unsetOperation();
		}
		else
		{
			getConfig().setInterface( operation.getInterface().getName() );
			getConfig().setOperation( operation.getName() );
		}

		this.operation = operation;

		notifyPropertyChanged( OPERATION_PROPERTY, oldOperation, operation );
	}

	public MockOperationDispatcher getMockOperationDispatcher()
	{
		return dispatcher;
	}

	@Override
	public void removeResponseFromConfig( int index )
	{
		getConfig().removeResponse( index );
	}

	private class InternalInterfaceListener extends InterfaceListenerAdapter
	{
		@Override
		public void operationUpdated( Operation operation )
		{
			// such wow - works? equals?
			if( operation == WsdlMockOperation.this.operation )
				getConfig().setOperation( operation.getName() );
		}

		@Override
		public void operationRemoved( Operation operation )
		{
			// such wow - works? equals?
			if( operation == WsdlMockOperation.this.operation )
				getMockService().removeMockOperation( WsdlMockOperation.this );
		}
	}

	private class InternalProjectListener extends ProjectListenerAdapter
	{
		@Override
		public void interfaceRemoved( Interface iface )
		{
			if( operation.getInterface() == iface )
				getMockService().removeMockOperation( WsdlMockOperation.this );
		}

		@Override
		public void interfaceUpdated( Interface iface )
		{
			if( operation.getInterface() == iface )
				getConfig().setInterface( iface.getName() );
		}
	}

	public boolean isOneWay()
	{
		return operation == null ? false : operation.isOneWay();
	}

	public boolean isNotification()
	{
		return operation == null ? false : operation.isNotification();
	}

	public boolean isSolicitResponse()
	{
		return operation == null ? false : operation.isSolicitResponse();
	}

	public boolean isUnidirectional()
	{
		return operation == null ? false : operation.isUnidirectional();
	}

	public boolean isBidirectional()
	{
		return !isUnidirectional();
	}

	public List<? extends ModelItem> getChildren()
	{
		return getMockResponses();
	}

	public void exportMockOperation( File file )
	{
		try
		{
			this.getConfig().newCursor().save( file );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}


}
