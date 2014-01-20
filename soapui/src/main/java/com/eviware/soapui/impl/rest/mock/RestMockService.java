package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.config.RESTMockServiceConfig;
import com.eviware.soapui.impl.support.AbstractMockService;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.model.mock.MockDispatcher;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.UISupport;

public class RestMockService extends AbstractMockService<RestMockAction, RESTMockServiceConfig>
{
	public RestMockService( Project project, RESTMockServiceConfig config )
	{
		super( config, project );

        if( !getConfig().isSetProperties() )
            getConfig().addNewProperties();

        setPropertiesConfig(config.getProperties());

    }

	@Override
	public void setPort( int port )
	{

	}

	@Override
	public void setPath( String path )
	{

	}

	@Override
	public MockDispatcher createDispatcher( WsdlMockRunContext mockContext )
	{
		return new RestMockDispatcher();
	}
}
