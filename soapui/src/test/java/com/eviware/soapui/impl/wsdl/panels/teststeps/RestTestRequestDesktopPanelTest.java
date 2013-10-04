/*
 * soapUI, copyright (C) 2004-2013 smartbear.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.panels.teststeps;

import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.RestRequestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.panels.request.views.content.RestRequestContentView;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.utils.ContainerWalker;
import com.eviware.soapui.utils.StatefulModelItemFactory;
import com.eviware.soapui.utils.StubbedDialogs;
import com.eviware.x.dialogs.XDialogs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RestTestRequestDesktopPanelTest
{

	public static final String PARAMETER_NAME = "jsessionid";
	public static final String PARAMETER_VALUE = "Da Value";
	public static final String ENDPOINT = "http://sunet.se/search";

	private RestTestRequestDesktopPanel restTestDesktopPanel;
	private RestRequest restRequest;
	private StubbedDialogs dialogs;
	private XDialogs originalDialogs;
	private JComboBox<String> endpointsCombo;

	@Before
	public void setUp() throws Exception
	{
		StatefulModelItemFactory modelItemFactory = new StatefulModelItemFactory();
		restRequest = modelItemFactory.makeRestRequest();
		restRequest.setMethod( RestRequestInterface.RequestMethod.GET );
		restRequest.getResource().getParams().addProperty( PARAMETER_NAME );
		restService().addEndpoint( ENDPOINT );
		restRequest.setEndpoint( ENDPOINT );
		RestParamProperty restParamProperty = restRequest.getParams().getProperty( PARAMETER_NAME );
		restParamProperty.setValue( PARAMETER_VALUE );
		TestStepConfig config = TestStepConfig.Factory.newInstance();
		RestRequestStepConfig requestStepConfig = RestRequestStepConfig.Factory.newInstance();
		requestStepConfig.setService( restService().getName() );
		RestRequestConfig restRequestConfig = RestRequestConfig.Factory.newInstance();
		requestStepConfig.setRestRequest( restRequestConfig );
		config.setConfig( requestStepConfig );
		requestStepConfig.setResourcePath( restRequest.getResource().getFullPath() );
		RestTestRequestStep testStep = new RestTestRequestStep( modelItemFactory.makeTestCase(), config, false );
		restTestDesktopPanel = new RestTestRequestDesktopPanel( testStep );
		originalDialogs = UISupport.getDialogs();
		dialogs = new StubbedDialogs();
		UISupport.setDialogs( dialogs );
		endpointsCombo = findEndpointsComboBox();
	}


	@After
	public void resetDialogs()
	{
		UISupport.setDialogs( originalDialogs );
	}

	@Test
	public void displaysEndpoint() {
		assertThat(restTestDesktopPanel.getEndpointsModel().getSelectedItem(), is((Object)ENDPOINT));
	}

	@Test
	public void displaysFullResourcePathInPathLabel() {
		assertThat(restTestDesktopPanel.pathLabel.getText(), is(restRequest.getResource().getFullPath()));
	}

	/* Helpers */

	private String getComboTextFieldValue() throws Exception
	{
		Document document = ( ( JTextComponent )endpointsCombo.getEditor().getEditorComponent() ).getDocument();
		return document.getText( 0, document.getLength() );
	}

	private JComboBox<String> findEndpointsComboBox()

	{
		ContainerWalker finder = new ContainerWalker( restTestDesktopPanel );
		return finder.findComboBoxWithValue( ENDPOINT );
	}

	private void setComboTextFieldValue( JComboBox<String> endpointsCombo, String otherValue )
	{
		(( JTextComponent ) endpointsCombo.getEditor().getEditorComponent()).setText(otherValue);
	}

	private void waitForSwingThread() throws InterruptedException
	{
		Thread.sleep( 50 );
	}

	private JTable getRestParameterTable()
	{
		List<? extends EditorView<? extends XmlDocument>> views = restTestDesktopPanel.getRequestEditor().getViews();
		RestRequestContentView restRequestContentView = ( RestRequestContentView )views.get( 0 );
		return restRequestContentView.getParamsTable().getParamsTable();
	}


	private RestService restService()
	{
		return restRequest.getOperation().getInterface();
	}
}
