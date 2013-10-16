package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.panels.request.views.content.RestRequestContentView;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.support.EndpointsComboBoxModel;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.utils.ContainerWalker;
import com.eviware.soapui.utils.ModelItemFactory;
import com.eviware.soapui.utils.StubbedDialogs;
import com.eviware.x.dialogs.XDialogs;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.eviware.soapui.utils.StubbedDialogs.hasPromptWithValue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;

/**
 * Unit tests for RestRequestDesktopPanel.
 */
public class RestRequestDesktopPanelTest
{

	public static final String PARAMETER_NAME = "jsessionid";
	public static final String PARAMETER_VALUE = "Da Value";
	public static final String ENDPOINT = "http://sunet.se/search";

	private RestRequestDesktopPanel requestDesktopPanel;
	private RestRequest restRequest;
	private StubbedDialogs dialogs;
	private XDialogs originalDialogs;
	private JComboBox<String> endpointsCombo;

	@Before
	public void setUp() throws Exception
	{
		restRequest = ModelItemFactory.makeRestRequest();
		restRequest.setMethod( RestRequestInterface.RequestMethod.GET );
		restRequest.getResource().getParams().addProperty( PARAMETER_NAME );
		restService().addEndpoint( ENDPOINT );
		restRequest.setEndpoint( ENDPOINT );
		RestParamProperty restParamProperty = restRequest.getParams().getProperty( PARAMETER_NAME );
		restParamProperty.setValue( PARAMETER_VALUE );
		requestDesktopPanel = new RestRequestDesktopPanel( restRequest );
		originalDialogs = UISupport.getDialogs();
		dialogs = new StubbedDialogs();
		UISupport.setDialogs( dialogs );
		endpointsCombo = findEndpointsComboBox();
	}

	@Test
	public void retainsParameterValueWhenChangingItsLevel() throws Exception
	{
		JTable paramsTable = getRestParameterTable();
		paramsTable.setValueAt( NewRestResourceActionBase.ParamLocation.METHOD, 0, 3 );
		paramsTable.setValueAt( NewRestResourceActionBase.ParamLocation.RESOURCE, 0, 3 );

		RestParamProperty returnedParameter = restRequest.getParams().getProperty( PARAMETER_NAME );
		assertThat( returnedParameter.getValue(), is( PARAMETER_VALUE ) );
	}

	@Test
	public void retainsParameterOrderWhenChangingItsLevel() throws Exception
	{
		restRequest.getParams().addProperty( "Param2" );
		getRestParameterTable().setValueAt( NewRestResourceActionBase.ParamLocation.METHOD, 0, 3 );

		assertThat( ( String )getRestParameterTable().getValueAt( 0, 0 ), is( PARAMETER_NAME ) );
	}

	@Test
	public void allowsRemovalOfParameterAfterParameterLevelChange() throws Exception
	{
		restRequest.getParams().addProperty( "Param2" );
		getRestParameterTable().setValueAt( NewRestResourceActionBase.ParamLocation.METHOD, 0, 3 );

		String paramNameAtRow0;
		restRequest.getParams().removeProperty( PARAMETER_NAME );
		paramNameAtRow0 = ( String )getRestParameterTable().getValueAt( 0, 0 );
		assertThat( paramNameAtRow0, is( "Param2" ) );

	}


	@Test
	public void displaysEndpoint()
	{
		assertThat( requestDesktopPanel.getEndpointsModel().getSelectedItem(), is( ( Object )ENDPOINT ) );
	}

	@Test
	public void reactsToEndpointChanges()
	{
		String anotherEndpoint = "http://mafia.ru/search";
		restService().changeEndpoint( ENDPOINT, anotherEndpoint );
		assertThat( requestDesktopPanel.getEndpointsModel().getSelectedItem(), is( ( Object )anotherEndpoint ) );
	}

	@Test
	public void keepsEnteredEndpointValueWhenEditingEndpoint() throws Exception
	{
		JComboBox<String> endpointsCombo = findEndpointsComboBox();
		String otherValue = "http://dn.se";
		setComboTextFieldValue( endpointsCombo, otherValue );
		endpointsCombo.setSelectedItem( EndpointsComboBoxModel.EDIT_ENDPOINT );

		waitForSwingThread();
		assertThat( dialogs.getPrompts(), hasPromptWithValue( otherValue ) );
	}

	@Test
	public void keepsEnteredEndpointValueWhenAddingNewEndpoint() throws Exception
	{
		String otherValue = "http://dn.se";
		setComboTextFieldValue( endpointsCombo, otherValue );
		endpointsCombo.setSelectedItem( EndpointsComboBoxModel.ADD_NEW_ENDPOINT );

		waitForSwingThread();
		assertThat( dialogs.getPrompts(), hasPromptWithValue( otherValue ) );
	}

	@Ignore( "For some reason this test fails, although it works fine in the GUI" )
	@Test
	public void resetsToEnteredValueWhenCancelingAdd() throws Exception
	{
		dialogs.mockPromptWithReturnValue( null );
		String otherValue = "http://dn.se";
		setComboTextFieldValue( endpointsCombo, otherValue );
		endpointsCombo.setSelectedItem( EndpointsComboBoxModel.ADD_NEW_ENDPOINT );

		waitForSwingThread();
		assertThat( getComboTextFieldValue(), is( otherValue ) );
	}

	@Test
	public void reactsToPathChanges()
	{
		String anotherPath = "/changed/path";
		restRequest.getResource().setPath( anotherPath );
		assertThat( requestDesktopPanel.resourcePanel.getText(), is( anotherPath ) );
	}

	@Ignore("Fails intermittently, but works in GUI")
	@Test
	public void parameterAdditionUpdatesParametersField() throws InterruptedException, InvocationTargetException
	{
		final String parameterName = "the_new_param";
		RestParamProperty newParameter = restRequest.getParams().addProperty( parameterName );
		newParameter.setStyle( RestParamsPropertyHolder.ParameterStyle.QUERY );
		final String value = "the_new_value";
		newParameter.setValue( value );

		assertThat( requestDesktopPanel.queryPanel.getText(), containsString( parameterName + "=" + value ) );
	}

	@After
	public void resetDialogs()
	{
		UISupport.setDialogs( originalDialogs );
	}

	/* Helpers */

	private String getComboTextFieldValue() throws Exception
	{
		Document document = ( ( JTextComponent )endpointsCombo.getEditor().getEditorComponent() ).getDocument();
		return document.getText( 0, document.getLength() );
	}

	private JComboBox<String> findEndpointsComboBox()
	{
		ContainerWalker finder = new ContainerWalker( requestDesktopPanel );
		return finder.findComboBoxWithValue( ENDPOINT );
	}

	private void setComboTextFieldValue( JComboBox<String> endpointsCombo, String otherValue )
	{
		( ( JTextComponent )endpointsCombo.getEditor().getEditorComponent() ).setText( otherValue );
	}

	private void waitForSwingThread() throws InterruptedException
	{
		Thread.sleep( 50 );
	}

	private JTable getRestParameterTable()
	{
		List<? extends EditorView<? extends XmlDocument>> views = requestDesktopPanel.getRequestEditor().getViews();
		RestRequestContentView restRequestContentView = ( RestRequestContentView )views.get( 0 );
		return restRequestContentView.getParamsTable().getParamsTable();
	}


	private RestService restService()
	{
		return restRequest.getOperation().getInterface();
	}
}
