package com.eviware.soapui.impl.wsdl.panels.request;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for WsdlRequestDesktopPanel, indirectly testing AbstractHttpRequestDesktopPanel as well.
 */
public class WsdlRequestDesktopPanelTest
{

	private WsdlRequestDesktopPanel desktopPanel;
	private WsdlRequest request;
	private ContainerWalker containerWalker;

	@BeforeClass
	public static void initSoapUICore()
	{
		DefaultSoapUICore.log = mock(Logger.class);
	}

	@Before
	public void setUp() throws Exception
	{
		request = mock( WsdlRequest.class );
		WsdlProject stubbedProject = mock( WsdlProject.class );
		when(stubbedProject.isEnvironmentMode()).thenReturn( true );
		when(request.getParent()).thenReturn( stubbedProject );
		XmlBeansSettingsImpl settings = mock( XmlBeansSettingsImpl.class );
		when(request.getSettings()).thenReturn( settings );
		desktopPanel = new WsdlRequestDesktopPanel( request );
		containerWalker = new ContainerWalker( desktopPanel );
	}

	@Test
	public void returnsCorrectHelpUrl() throws Exception
	{
		assertEquals( HelpUrls.REQUESTEDITOR_HELP_URL, desktopPanel.getHelpUrl());
	}

	@Test
	public void returnsRequest() throws Exception
	{
		assertSame(request, desktopPanel.getRequest());
	}

	@Test
	public void disablesInteractionsDuringSubmit() throws Exception
	{
		Submit submit = makeSubmitMockWithRequest();
		desktopPanel.beforeSubmit( submit, mock( SubmitContext.class) );

		assertFalse("Should disable Submit button", desktopPanel.getSubmitButton().isEnabled());
		assertFalse("Should disable Create empty button", containerWalker.findButtonWithIcon( "create_empty_request.gif" ).isEnabled());
		assertFalse("Should disable Clone button", containerWalker.findButtonWithIcon( "clone_request.gif" ).isEnabled());
	}

	@Test
	public void reenablesInteractionsAfterSubmit() throws Exception
	{
		Submit submit = makeSubmitMockWithRequest();
		SubmitContext submitContext = mock( SubmitContext.class );
		desktopPanel.beforeSubmit( submit, submitContext );
		desktopPanel.afterSubmit( submit, submitContext );

		assertTrue("Create empty button should be enabled", containerWalker.findButtonWithIcon( "create_empty_request.gif" ).isEnabled());
		assertTrue("Clone button should be enabled", containerWalker.findButtonWithIcon( "clone_request.gif" ).isEnabled());

	}



	private Submit makeSubmitMockWithRequest()
	{
		Submit submit = mock( Submit.class );
		when(submit.getRequest()).thenReturn( request );
		return submit;
	}


}
