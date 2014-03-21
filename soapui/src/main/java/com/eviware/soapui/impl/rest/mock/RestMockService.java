package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.config.PropertyConfig;
import com.eviware.soapui.config.RESTMockActionConfig;
import com.eviware.soapui.config.RESTMockServiceConfig;
import com.eviware.soapui.impl.rest.HttpMethod;
import com.eviware.soapui.impl.rest.RestMethod;
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

import java.util.*;

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


    public RestMockAction addEmptyMockAction(RestRequestInterface.HttpMethod method, String path)
	{
		RESTMockActionConfig config = getConfig().addNewRestMockAction();

      String slashifiedPath = slashify(path);
		String name = path ;

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

	protected MockOperation findMatchingOperation( String pathToFind, RestRequestInterface.HttpMethod verbToFind )
	{
		for( MockOperation operation : getMockOperationList() )
		{
			String operationPath = ( ( RestMockAction )operation ).getResourcePath();
			RestRequestInterface.HttpMethod operationVerb = ( ( RestMockAction )operation ).getMethod();

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

		RestRequestInterface.HttpMethod httpMethod = RestRequestInterface.HttpMethod.GET;
		String path = restResource.getFullPath();

		if(restResource.getRestMethodCount() > 0 )
		{
			return addNewMockOperationFromRestMethod( restResource.getRestMethodAt( 0 ), httpMethod, path );
		}

		return addEmptyMockAction( httpMethod, path );
	}

	public List<MockOperation> addNewMockOperationFromService( RestResource restResource )
	{
		List<MockOperation> actions = new ArrayList<MockOperation>();

		if( restResource.getRestMethodCount() < 1)
		{
			actions.add( addNewMockOperation( restResource ) );
		}

		for( RestMethod restMethod: restResource.getRestMethodList())
		{
			HttpMethod httpMethod = restMethod.getMethod();
			String path = restResource.getFullPath();

			RestMockAction mockAction = addNewMockOperationFromRestMethod( restMethod, httpMethod, path );

			actions.add( mockAction );
		}

		return actions;
	}

	private RestMockAction addNewMockOperationFromRestMethod( RestMethod restMethod, HttpMethod defaultHttpMethod, String defaultPath )
	{
		if( restMethod.getRequestCount() > 0 )
		{
			RestRequest request = restMethod.getRequestAt( 0 );
			return addEmptyMockAction( request.getMethod(), request.getTemplateParamExpandedPath() );
		}
		return addEmptyMockAction( defaultHttpMethod, defaultPath );
	}

	private String slashify( String path )
    {
        if( !path.startsWith( "/" ))
            return "/" + path;
        return path;
    }

}
