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

package com.eviware.soapui.impl.wsdl.teststeps;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.support.InterfaceListenerAdapter;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.resolver.ChangeOperationResolver;
import com.eviware.soapui.support.resolver.ImportInterfaceResolver;
import com.eviware.soapui.support.resolver.RemoveTestStepResolver;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.resolver.ResolveContext.PathToResolve;

public class RestTestRequestStep extends HttpTestRequestStep
{
	private final static Logger log = Logger.getLogger( RestTestRequestStep.class );
	private RestResource restResource;
	private final InternalProjectListener projectListener = new InternalProjectListener();
	private final InternalInterfaceListener interfaceListener = new InternalInterfaceListener();

	public RestTestRequestStep( WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest )
	{
		super( testCase, config, forLoadTest );

		restResource = findRestResource();
		initRestTestRequest();

		if( !forLoadTest && restResource != null )
		{
			restResource.getInterface().getProject().addProjectListener( projectListener );
			restResource.getInterface().addInterfaceListener( interfaceListener );

			// we need to listen for name changes which happen when interfaces are
			restResource.getInterface().addPropertyChangeListener( this );
			restResource.addPropertyChangeListener( this );
		}
	}

	private void initRestTestRequest()
	{
		if( restResource == null )
			setDisabled( true );
		else
			getTestRequest().setResource( restResource );
	}

	public String getService()
	{
		return getRequestStepConfig().getService();
	}

	public String getResourcePath()
	{
		return getRequestStepConfig().getResourcePath();
	}

	protected String createDefaultRawResponseContent()
	{
		return restResource == null ? null : restResource.createResponse( true );
	}

	protected String createDefaultResponseXmlContent()
	{
		return restResource == null ? null : restResource.createResponse( true );
	}

	protected String createDefaultRequestContent()
	{
		return restResource == null ? null : restResource.createRequest( true );
	}

	@Override
	public Collection<Interface> getRequiredInterfaces()
	{
		ArrayList<Interface> result = new ArrayList<Interface>();
		result.add( findRestResource().getInterface() );
		return result;
	}

	private RestResource findRestResource()
	{
		Project project = ModelSupport.getModelItemProject( this );
		RestService restService = ( RestService )project.getInterfaceByName( getRequestStepConfig().getService() );
		if( restService != null )
		{
			return restService.getResourceByFullPath( getRequestStepConfig().getResourcePath() );
		}

		return null;
	}

	public RestResource getResource()
	{
		return restResource;
	}

	@Override
	public void release()
	{
		super.release();

		if( restResource != null )
		{
			restResource.removePropertyChangeListener( this );
			restResource.getInterface().getProject().removeProjectListener( projectListener );
			restResource.getInterface().removeInterfaceListener( interfaceListener );
			restResource.getInterface().removePropertyChangeListener( this );
		}
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getSource() == restResource )
		{
			if( evt.getPropertyName().equals( RestResource.PATH_PROPERTY ) )
			{
				getRequestStepConfig().setResourcePath( restResource.getFullPath() );
			}
		}
		else if( restResource != null && evt.getSource() == restResource.getInterface() )
		{
			if( evt.getPropertyName().equals( Interface.NAME_PROPERTY ) )
			{
				getRequestStepConfig().setService( ( String )evt.getNewValue() );
			}
		}

		super.propertyChange( evt );
	}

	public class InternalProjectListener extends ProjectListenerAdapter
	{
		@Override
		public void interfaceRemoved( Interface iface )
		{
			if( restResource != null && restResource.getInterface().equals( iface ) )
			{
				log.debug( "Removing test step due to removed interface" );
				( getTestCase() ).removeTestStep( RestTestRequestStep.this );
			}
		}
	}

	public class InternalInterfaceListener extends InterfaceListenerAdapter
	{
		@Override
		public void operationRemoved( Operation operation )
		{
			if( operation == restResource )
			{
				log.debug( "Removing test step due to removed operation" );
				( getTestCase() ).removeTestStep( RestTestRequestStep.this );
			}
		}

		@Override
		public void operationUpdated( Operation operation )
		{
			if( operation == restResource )
			{
				// requestStepConfig.setResourcePath( operation.get );
			}
		}
	}

	@Override
	public boolean dependsOn( AbstractWsdlModelItem<?> modelItem )
	{
		if( modelItem instanceof Interface && getTestRequest().getOperation() != null
				&& getTestRequest().getOperation().getInterface() == modelItem )
		{
			return true;
		}
		else if( modelItem instanceof Operation && getTestRequest().getOperation() == modelItem )
		{
			return true;
		}

		return false;
	}

	public void setResource( RestResource operation )
	{
		if( restResource == operation )
			return;

		RestResource oldOperation = restResource;
		restResource = operation;
		getRequestStepConfig().setService( operation.getInterface().getName() );
		getRequestStepConfig().setResourcePath( operation.getFullPath() );

		if( oldOperation != null )
			oldOperation.removePropertyChangeListener( this );

		restResource.addPropertyChangeListener( this );
		getTestRequest().setResource( restResource );
	}

	public Interface getInterface()
	{
		return restResource == null ? null : restResource.getInterface();
	}

	public TestStep getTestStep()
	{
		return this;
	}

	@SuppressWarnings( "unchecked" )
	public void resolve( ResolveContext context )
	{
		super.resolve( context );

		if( restResource == null )
		{
			if( context.hasThisModelItem( this, "Missing REST Resource in Project", getRequestStepConfig().getService()
					+ "/" + getRequestStepConfig().getResourcePath() ) )
				return;
			context.addPathToResolve( this, "Missing REST Resource in Project",
					getRequestStepConfig().getService() + "/" + getRequestStepConfig().getResourcePath() ).addResolvers(
					new RemoveTestStepResolver( this ), new ImportInterfaceResolver( this )
					{
						@Override
						protected boolean update()
						{
							RestResource restResource = findRestResource();
							if( restResource == null )
								return false;

							setResource( restResource );
							initRestTestRequest();
							setDisabled( false );
							return true;
						}

					}, new ChangeOperationResolver( this, "Resource" )
					{
						@Override
						public boolean update()
						{
							RestResource restResource = ( RestResource )getSelectedOperation();
							if( restResource == null )
								return false;

							setResource( restResource );
							initRestTestRequest();
							setDisabled( false );
							return true;
						}

						protected Interface[] getInterfaces( WsdlProject project )
						{
							List<RestService> interfaces = ModelSupport.getChildren( project, RestService.class );
							return interfaces.toArray( new Interface[interfaces.size()] );
						}
					} );
		}
		else
		{
			restResource.resolve( context );
			if( context.hasThisModelItem( this, "Missing REST Resource in Project", getRequestStepConfig().getService()
					+ "/" + getRequestStepConfig().getResourcePath() ) )
			{
				PathToResolve path = context.getPath( this, "Missing REST Resource in Project", getRequestStepConfig()
						.getService()
						+ "/" + getRequestStepConfig().getResourcePath() );
				path.setSolved( true );
			}
		}
	}
}
