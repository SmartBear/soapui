package com.eviware.soapui.impl.actions;

import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.rest.*;
import com.eviware.soapui.impl.rest.support.*;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;

import java.net.MalformedURLException;

public class RestServiceBuilder
{

	public void createRestService( WsdlProject project, String URI ) throws MalformedURLException
	{
		if( StringUtils.isNullOrEmpty( URI ) )
		{
			return;
		}

		RestResource restResource = createResource( project, URI);
		RestRequest restRequest = addNewRequest( addNewMethod( restResource ) );
		copyParameters( extractParams( URI ), restResource.getParams() );
		UISupport.select( restRequest );
		UISupport.showDesktopPanel( restRequest );

	}

	public void createRestServiceHeadless(WsdlProject project, String URI) throws MalformedURLException
	{
		if( StringUtils.isNullOrEmpty( URI ) )
		{
			return;
		}

		RestResource restResource = createResource( project, URI);
		RestRequest restRequest = addNewRequest( addNewMethod( restResource ) );
		copyParametersWithDefaultsOnResource( extractParams( URI ), restResource.getParams(),restRequest.getParams() );
	}

	private RestParamsPropertyHolder extractParams( String URI )
	{
		RestParamsPropertyHolder params = new XmlBeansRestParamsTestPropertyHolder( null,
				RestParametersConfig.Factory.newInstance() );
		extractAndFillParameters( URI, params );
		return params;
	}

	private RestResource createResource( WsdlProject project, String URI) throws MalformedURLException
	{
		RestURIParser restURIParser = new RestURIParserImpl( URI );
		String resourcePath = restURIParser.getResourcePath();
		String host = restURIParser.getEndpoint();

		RestService restService = ( RestService )project.addNewInterface( host, RestServiceFactory.REST_TYPE );
		restService.addEndpoint( restURIParser.getEndpoint() );
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


	protected RestMethod addNewMethod( RestResource restResource )
	{
		RestMethod restMethod = restResource.addNewMethod( restResource.getName());
		restMethod.setMethod( RestRequestInterface.RequestMethod.GET );
		return restMethod;
	}

	protected RestRequest addNewRequest( RestMethod restMethod )
	{
		return restMethod.addNewRequest( "Request " + ( restMethod.getRequestCount() + 1 ) );
	}
}
