package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.config.PropertyConfig;
import com.eviware.soapui.config.RESTMockActionConfig;
import com.eviware.soapui.config.RESTMockServiceConfig;
import com.eviware.soapui.impl.rest.HttpMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.support.AbstractMockService;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockDispatcher;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.project.Project;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RestMockService extends AbstractMockService<RestMockAction, RestMockResponse, RESTMockServiceConfig>
{

	public final static String ICON_NAME = "/restMockService.gif";

	public RestMockService( Project project, RESTMockServiceConfig config )
	{
		super( config, project, ICON_NAME );

		List<RESTMockActionConfig> restActionConfigList = config.getRestMockActionList();
		for( RESTMockActionConfig restActionConfig : restActionConfigList )
		{
			RestMockAction restMockAction = new RestMockAction( this, restActionConfig );
			addMockOperation( restMockAction );
		}

		if( !getConfig().isSetProperties() )
			getConfig().addNewProperties();

		setPropertiesConfig( config.getProperties() );

	}

	@Override
	public String getIconName()
	{
		return ICON_NAME;
	}

	@Override
	public MockDispatcher createDispatcher( WsdlMockRunContext mockContext )
	{
		return new RestMockDispatcher( this, mockContext );
	}

	public RestMockAction addNewMockAction( RestRequest restRequest )
	{
        Map<String, String> pathParametersMap = getPathParametersMap(restRequest.getResource(), restRequest.getPath());
        RestMockAction mockAction = addEmptyMockAction( restRequest.getMethod(), restRequest.getPath(), pathParametersMap );
		mockAction.setResource( restRequest.getResource() );

		return mockAction;
	}

    public RestMockAction addEmptyMockAction( HttpMethod method, String path )
    {
        return addEmptyMockAction( method, path, null );
    }

    protected RestMockAction addEmptyMockAction( HttpMethod method, String path, Map<String, String> pathParams )
	{
		RESTMockActionConfig config = getConfig().addNewRestMockAction();

        if( pathParams != null )
        {
            addPathParamsToProperty(pathParams, config);
        }

        String slashifiedPath = slashify(path);
		String name = lastPartOf( path );

		config.setName( name );
		config.setMethod( method.name() );
		config.setResourcePath( slashifiedPath );
		RestMockAction restMockAction = new RestMockAction( this, config );

		addMockOperation( restMockAction );
		fireMockOperationAdded( restMockAction );

		return restMockAction;
	}

    private void addPathParamsToProperty(Map<String, String> pathParams, RESTMockActionConfig config)
    {
        Iterator<Map.Entry<String,String>> iterator = pathParams.entrySet().iterator();

        while ( iterator.hasNext() )
        {
            Map.Entry<String, String> pathParamEntry = iterator.next();
            PropertyConfig propertyConfig = config.addNewProperty();
            propertyConfig.setName( pathParamEntry.getKey() );
            propertyConfig.setValue( pathParamEntry.getValue() );
        }
    }

    private String lastPartOf( String path )
	{
		String[] parts = path.split( "/" );
		if( parts.length == 0 )
		{
			return "";
		}
		return parts[parts.length - 1];
	}

	private String slashify( String path )
	{
		if( !path.startsWith( "/" ))
			return "/" + path;
		return path;
	}

	public MockOperation findOrCreateNewOperation( RestRequest restRequest )
	{
		MockOperation matchedOperation = findMatchingOperation( restRequest.getPath(), restRequest.getMethod() );

		if( matchedOperation == null)
		{
			matchedOperation = addNewMockAction( restRequest );
		}
		return matchedOperation;
	}

	protected MockOperation findMatchingOperation( String pathToFind, HttpMethod verbToFind )
	{
		for( MockOperation operation : getMockOperationList() )
		{
			String operationPath = ( ( RestMockAction )operation ).getResourcePath();
			HttpMethod operationVerb = ( ( RestMockAction )operation ).getMethod();

			boolean matchesPath = operationPath.equals( pathToFind );
			boolean matchesVerb = verbToFind == operationVerb;

			if( matchesPath && matchesVerb )
			{
				return operation;
			}
		}

		return null;
	}

	public boolean canIAddAMockOperation( RestMockAction mockOperation )
	{
		return this.getConfig().getRestMockActionList().contains( mockOperation.getConfig() );
	}

	@Override
	public MockOperation addNewMockOperation( Operation operation )
	{
		RestResource restResource = (RestResource)operation;

		HttpMethod httpMethod = HttpMethod.GET;
		String path = restResource.getPath();

		if( restResource.getRequestCount() > 0 )
		{
			RestRequest request = restResource.getRequestAt( 0 );
			httpMethod = request.getMethod();
			path = path + request.getPath();
		}


        Map<String, String> pathParametersMap = getPathParametersMap(restResource, path);

        return addEmptyMockAction( httpMethod, path, pathParametersMap );
	}

    private Map<String, String> getPathParametersMap(RestResource restResource, String path) {
        Map<String, String> pathParameters = new HashMap<String, String>();
        for(String pathParam: RestUtils.extractTemplateParams(path))
        {
            String pathParamValue = restResource.getParams().getPropertyValue( pathParam );
            pathParameters.put( pathParam, pathParamValue );

        }
        return pathParameters;
    }

}
