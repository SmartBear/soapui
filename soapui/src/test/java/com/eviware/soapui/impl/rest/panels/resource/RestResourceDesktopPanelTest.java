package com.eviware.soapui.impl.rest.panels.resource;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.panels.request.RestRequestDesktopPanel;
import com.eviware.soapui.impl.rest.panels.request.views.content.RestRequestContentView;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.utils.ModelItemFactory;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.util.List;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation.METHOD;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: Prakash
 * Date: 2013-10-01
 * Time: 09:09
 * To change this template use File | Settings | File Templates.
 */
public class RestResourceDesktopPanelTest
{
	static final String PARAM_2 = "Param2";
	static final String PARAM_3 = "Param3";
	static final String PARAM_1 = "Param1";

	private RestResource restResource;
	private RestResourceDesktopPanel restResourceDesktopPanel;

	@Before
	public void setUp() throws SoapUIException
	{
		restResource = ModelItemFactory.makeRestResource();

		RestParamsPropertyHolder params = restResource.getParams();
		params.addProperty( PARAM_1 );
		params.addProperty( PARAM_2 );
		params.addProperty( PARAM_3 );

		restResourceDesktopPanel = new RestResourceDesktopPanel( restResource );
	}

	@Test
	public void allowsToDeleteParameterAfterParameterLevelChange() throws SoapUIException
	{
		RestParamsPropertyHolder params = restResource.getParams();

		JTable parameterTable = getRestParameterTable();
		RestRequestDesktopPanel requestDesktopPanel =
				openRestRequestDesktopPanelToAttachTheEventListenersForParameterlevelChange();

		//Param1 to Method, and hence it should be removed
		parameterTable.setValueAt( METHOD, 0, RestParamsTableModel.PARAM_LOCATION_COLUMN_INDEX );
		params.removeProperty(PARAM_2 );

		String remainingParam3 =  (String)parameterTable.getValueAt( 0, 0 );
		assertThat( remainingParam3, Is.is( PARAM_3 ) );
	}

	@Test
	public void doesNotMoveTheParameterToMethodOnParamLocationChangeToMethodWhenListenersNotAttached()
			throws SoapUIException
	{
		JTable parameterTable = getRestParameterTable();
		//Change the level but it will not  really move it as listeners are not registered
		parameterTable.setValueAt( METHOD, 0, RestParamsTableModel.PARAM_LOCATION_COLUMN_INDEX );
		String param1StillExists =  (String)parameterTable.getValueAt( 0, 0 );
		assertThat( param1StillExists, Is.is( PARAM_1) );
	}

	/**
	 * This should be fixed as param location change listener should be present at each level
	 * @return
	 * @throws SoapUIException
	 */
	private RestRequestDesktopPanel openRestRequestDesktopPanelToAttachTheEventListenersForParameterlevelChange()
			throws SoapUIException
	{
		RestRequest restRequest = ModelItemFactory.makeRestRequest( restResource );
		restRequest.setMethod( RestRequestInterface.RequestMethod.GET );
		return new RestRequestDesktopPanel( restRequest );
	}

	private JTable getRestParameterTable()
	{
		return restResourceDesktopPanel.getParamsTable().getParamsTable();
	}
}
