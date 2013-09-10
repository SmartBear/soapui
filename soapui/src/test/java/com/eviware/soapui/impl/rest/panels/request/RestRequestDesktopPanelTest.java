package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.panels.request.views.content.RestRequestContentView;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JTable;
import java.util.List;

import static com.eviware.soapui.utils.ModelItemFactory.makeRestMethod;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for RestRequestDesktopPanel.
 */
public class RestRequestDesktopPanelTest
{

	public static final String PARAMETER_NAME = "jsessionid";
	public static final String PARAMETER_VALUE = "Da Value";
	private RestRequestDesktopPanel requestDesktopPanel;
	private RestRequest restRequest;

	@Before
	public void setUp() throws Exception
	{
		restRequest = new RestRequest( makeRestMethod(), RestRequestConfig.Factory.newInstance(), false );
		restRequest.setMethod( RestRequestInterface.RequestMethod.GET);
		restRequest.getResource().getParams().addProperty( PARAMETER_NAME );
		RestParamProperty restParamProperty = restRequest.getParams().getProperty( PARAMETER_NAME );
		restParamProperty.setValue( PARAMETER_VALUE );
		requestDesktopPanel = new RestRequestDesktopPanel( restRequest );
	}

	@Test
	public void retainsParameterValueWhenChangingItsLevel() throws Exception
	{
		List<? extends EditorView<? extends XmlDocument>> views = requestDesktopPanel.getRequestEditor().getViews();
		RestRequestContentView restRequestContentView = ( RestRequestContentView )views.get( 0 );
		JTable paramsTable = restRequestContentView.getParamsTable().getParamsTable();
		paramsTable.setValueAt( NewRestResourceActionBase.ParamLocation.METHOD, 0, 3 );
		paramsTable.setValueAt( NewRestResourceActionBase.ParamLocation.RESOURCE, 0, 3 );

		RestParamProperty returnedParameter = restRequest.getParams().getProperty( PARAMETER_NAME );
		assertThat(returnedParameter.getValue(), is(PARAMETER_VALUE));
	}
}
