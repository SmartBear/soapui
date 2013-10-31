package com.eviware.soapui.impl.rest.panels.resource;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.panels.request.RestRequestDesktopPanel;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.JTable;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation.METHOD;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the RestResourceDesktopPanel class.
 */
public class RestResourceDesktopPanelTest
{
	static final String PARAM_2 = "Param2";
	static final String PARAM_3 = "Param3";
	static final String PARAM_1 = "Param1";

	private RestResource parentResource;
	private RestResource childResource;
	private RestResourceDesktopPanel resourceDesktopPanel;

	@Before
	public void setUp() throws Exception
	{

		parentResource = ModelItemFactory.makeRestResource();
		parentResource.setPath( "/parent" );

		childResource = parentResource.addNewChildResource( "child", "the_child" );
		resourceDesktopPanel = new RestResourceDesktopPanel( childResource );
	}

	@Test
	public void displaysFullPathForChildResource() throws Exception
	{
		assertThat( resourceDesktopPanel.pathTextField.getText(), is( childResource.getFullPath() ));
	}

	@Ignore("Fix now, or later?")
	@Test
	public void allowsToDeleteParameterAfterParameterLevelChange() throws SoapUIException
	{
		RestParamsPropertyHolder params = addParamsToChildResource();

		JTable parameterTable = getRestParameterTable();
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
		RestParamsPropertyHolder params = addParamsToChildResource();
		JTable parameterTable = getRestParameterTable();
		//Change the level but it will not  really move it as listeners are not registered
		parameterTable.setValueAt( METHOD, 0, RestParamsTableModel.PARAM_LOCATION_COLUMN_INDEX );
		String param1StillExists =  (String)parameterTable.getValueAt( 0, 0 );
		assertThat( param1StillExists, Is.is( PARAM_1 ) );
	}

	/**
	 * This should be fixed as param location change listener should be present at each level
	 * @return
	 * @throws SoapUIException
	 */
	private RestRequestDesktopPanel openRestRequestDesktopPanelToAttachTheEventListenersForParameterlevelChange()
			throws SoapUIException
	{
		RestRequest restRequest = ModelItemFactory.makeRestRequest( childResource );
		restRequest.setMethod( RestRequestInterface.RequestMethod.GET );
		return new RestRequestDesktopPanel( restRequest );
	}

	private RestParamsPropertyHolder addParamsToChildResource()
	{
		RestParamsPropertyHolder params = childResource.getParams();
		params.addProperty( PARAM_1 );
		params.addProperty( PARAM_2 );
		params.addProperty( PARAM_3 );
		return params;
	}

	private JTable getRestParameterTable()
	{
		return resourceDesktopPanel.getParamsTable().getParamsTable();
	}
}
