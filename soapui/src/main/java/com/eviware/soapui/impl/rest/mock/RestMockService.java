package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.config.RESTMockActionConfig;
import com.eviware.soapui.config.RESTMockServiceConfig;
import com.eviware.soapui.impl.rest.HttpMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.support.AbstractMockService;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockDispatcher;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.project.Project;

import java.util.List;

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
        RestMockAction mockAction = addEmptyMockAction( restRequest.getMethod(),
                                                        restRequest.getTemplateParamExpandedPath() );
		mockAction.setResource( restRequest.getResource() );

		return mockAction;
	}


    public RestMockAction addEmptyMockAction(HttpMethod method, String path)
	{
		RESTMockActionConfig config = getConfig().addNewRestMockAction();

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

        return addEmptyMockAction( httpMethod, path );
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

	@Override
	public String getHelpUrl()
	{
		return HelpUrls.REST_MOCKSERVICE_HELP_URL;
	}
}
