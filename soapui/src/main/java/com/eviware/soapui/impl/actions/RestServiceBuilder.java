package com.eviware.soapui.impl.actions;

import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.rest.*;
import com.eviware.soapui.impl.rest.support.*;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import org.apache.commons.lang.ArrayUtils;

import java.net.MalformedURLException;

public class RestServiceBuilder
{

	public void createRestService( WsdlProject project, String URI ) throws MalformedURLException
	{
		if( StringUtils.isNullOrEmpty( URI ) )
		{
			return;
		}

		RestResource restResource = createResource( ModelCreationStrategy.CREATE_NEW_MODEL, project, URI );
		RestRequest restRequest = addNewRequest( addNewMethod( ModelCreationStrategy.CREATE_NEW_MODEL, restResource, RestRequestInterface.RequestMethod.GET ) );
		copyParameters( extractParams( URI ), restResource.getParams() );
		UISupport.select( restRequest );
		UISupport.showDesktopPanel( restRequest );

	}

	protected RestParamsPropertyHolder extractParams( String URI )
	{
		RestParamsPropertyHolder params = new XmlBeansRestParamsTestPropertyHolder( null,
				RestParametersConfig.Factory.newInstance() );
		extractAndFillParameters( URI, params );
		return params;
	}

	protected RestResource createResource( ModelCreationStrategy creationStrategy, WsdlProject project, String URI ) throws MalformedURLException
	{
		RestURIParser restURIParser = new RestURIParserImpl( URI );
		String resourcePath = restURIParser.getResourcePath();
		String host = restURIParser.getEndpoint();

		RestService restService = null;
		if( creationStrategy == ModelCreationStrategy.REUSE_MODEL )
		{
			AbstractInterface<?> existingInterface = project.getInterfaceByName( host );
			if( existingInterface instanceof RestService && ArrayUtils.contains( existingInterface.getEndpoints(), host ) )
			{
				restService = ( RestService )existingInterface;
			}
		}
		if( restService == null )
		{
			restService = ( RestService )project.addNewInterface( host, RestServiceFactory.REST_TYPE );
			restService.addEndpoint( restURIParser.getEndpoint() );
		}
		if( creationStrategy == ModelCreationStrategy.REUSE_MODEL )
		{
			RestResource existingResource = restService.getResourceByFullPath( RestResource.removeMatrixParams(resourcePath) );
			if( existingResource != null )
			{
				return existingResource;
			}
		}
		return restService.addNewResource( restURIParser.getResourceName(), resourcePath );
	}

	protected void extractAndFillParameters( String URI, RestParamsPropertyHolder params )
	{
		// This does lot of magic including extracting and filling up parameters on the params
		RestUtils.extractParams( URI, params, false, RestUtils.TemplateExtractionOption.EXTRACT_TEMPLATE_PARAMETERS );
	}

	//TODO: In advanced version we have to apply filtering like which type of parameter goes to which location
	protected void copyParameters( RestParamsPropertyHolder srcParams, RestParamsPropertyHolder destinationParams )
	{
		for( int i = 0; i < srcParams.size(); i++ )
		{
			RestParamProperty prop = srcParams.getPropertyAt( i );

			destinationParams.addParameter( prop );

		}
	}

	//TODO: In advanced version we have to apply filtering like which type of parameter goes to which location
	protected void copyParametersWithDefaultsOnResource( RestParamsPropertyHolder srcParams, RestParamsPropertyHolder resourceParams, RestParamsPropertyHolder requestParams )
	{
		for( int i = 0; i < srcParams.size(); i++ )
		{
			RestParamProperty prop = srcParams.getPropertyAt( i );
			String value = prop.getValue();
			prop.setValue( "" );
			prop.setDefaultValue( "" );
			resourceParams.addParameter( prop );

			requestParams.getProperty( prop.getName() ).setValue( value );
		}
	}


	protected RestMethod addNewMethod( ModelCreationStrategy creationStrategy, RestResource restResource, RestRequestInterface.RequestMethod requestMethod )
	{
		if( creationStrategy == ModelCreationStrategy.REUSE_MODEL )
		{
			for( RestMethod restMethod : restResource.getRestMethodList() )
			{
				if( restMethod.getMethod() == requestMethod )
				{
					return restMethod;
				}
			}
		}
		RestMethod restMethod = restResource.addNewMethod( restResource.getName() );
		restMethod.setMethod( requestMethod );
		return restMethod;
	}

	protected RestRequest addNewRequest( RestMethod restMethod )
	{
		return restMethod.addNewRequest( "Request " + ( restMethod.getRequestCount() + 1 ) );
	}

	protected static enum ModelCreationStrategy
	{
		CREATE_NEW_MODEL, REUSE_MODEL
	}
}
