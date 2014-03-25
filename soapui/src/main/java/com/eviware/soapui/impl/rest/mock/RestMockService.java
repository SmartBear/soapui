package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.config.RESTMockActionConfig;
import com.eviware.soapui.config.RESTMockServiceConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.support.AbstractMockService;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockDispatcher;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.project.Project;
import static com.eviware.soapui.impl.rest.RestRequestInterface.*;

import java.util.ArrayList;
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
				RestUtils.getTemplateParamExpandedPath( restRequest.getPath(), restRequest.getParams(), restRequest ) );
		mockAction.setResource( restRequest.getResource() );

		return mockAction;
	}


    public RestMockAction addEmptyMockAction(HttpMethod method, String path)
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
		String expandedPath = RestUtils.getTemplateParamExpandedPath( restRequest.getPath(), restRequest.getParams(), restRequest );

		MockOperation matchedOperation = findBestMatchingOperation( expandedPath, restRequest.getMethod(), false );

		if( matchedOperation == null)
		{
			matchedOperation = addNewMockAction( restRequest );
		}
		return matchedOperation;
	}

	protected MockOperation findBestMatchingOperation( String pathToFind, HttpMethod verbToFind, boolean includePartialMatch )
	{
		MockOperation bestMatchedOperation = null;

		for( MockOperation operation : getMockOperationList() )
		{
			String operationPath = ( ( RestMockAction )operation ).getResourcePath();
			HttpMethod operationVerb = ( ( RestMockAction )operation ).getMethod();

			boolean matchesPath = operationPath.equals( pathToFind );
			boolean matchesVerb = verbToFind == operationVerb;
			boolean matchesPathPartially = pathToFind.contains( operationPath );

			if( matchesPath && matchesVerb )
			{
				return operation;
			}
			else if ( includePartialMatch && matchesPathPartially && matchesVerb )
			{
				if( bestMatchedOperation == null ||  (( RestMockAction )bestMatchedOperation).getResourcePath().length() < operationPath.length()  )
				{
					bestMatchedOperation = operation;
				}
			}
		}

		return bestMatchedOperation;
	}

	public boolean canIAddAMockOperation( RestMockAction mockOperation )
	{
		return this.getConfig().getRestMockActionList().contains( mockOperation.getConfig() );
	}

	@Override
	public MockOperation addNewMockOperation( Operation operation )
	{
		return addNewMockOperationsFromResource( ( RestResource )operation ).get( 0 );
	}

	public List<MockOperation> addNewMockOperationsFromResource( RestResource restResource )
	{
		List<MockOperation> actions = new ArrayList<MockOperation>();
		String path = RestUtils.getTemplateParamExpandedPath( restResource.getFullPath(), restResource.getParams(), restResource );

		if( restResource.getRestMethodCount() < 1)
		{
			actions.add( addEmptyMockAction( HttpMethod.GET, path ) );
		}

		for( RestMethod restMethod: restResource.getRestMethodList())
		{
			actions.add( addEmptyMockAction( restMethod.getMethod(), path ) );
		}

		return actions;
	}

	private String slashify( String path )
    {
       if( !path.startsWith( "/" ) && !path.isEmpty() )
		 {
			 return "/" + path;
		 }

       return path;
    }

	@Override
	public String getHelpUrl()
	{
		return HelpUrls.REST_MOCKSERVICE_HELP_URL;
	}


}
