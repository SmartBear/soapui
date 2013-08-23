package com.eviware.soapui.impl.wsdl.panels.request;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.utils.ContainerWalker;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.eviware.soapui.utils.SwingMatchers.enabled;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
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
	public static void initSoapUICoreLog()
	{
		DefaultSoapUICore.log = mock( Logger.class );
	}

	@Before
	public void setUp() throws Exception
	{
		request = mock( WsdlRequest.class );
		WsdlProject stubbedProject = mock( WsdlProject.class );
		when( stubbedProject.isEnvironmentMode() ).thenReturn( true );
		when( request.getParent() ).thenReturn( stubbedProject );
		XmlBeansSettingsImpl settings = mock( XmlBeansSettingsImpl.class );
		when( request.getSettings() ).thenReturn( settings );
		desktopPanel = new WsdlRequestDesktopPanel( request );
		containerWalker = new ContainerWalker( desktopPanel );
	}

	@Test
	public void returnsCorrectHelpUrl() throws Exception
	{
		assertThat( desktopPanel.getHelpUrl(), is(HelpUrls.REQUESTEDITOR_HELP_URL) );
	}

	@Test
	public void returnsRequest() throws Exception
	{
		assertThat( desktopPanel.getRequest(), is( request ) );
	}

	@Test
	public void disablesInteractionsDuringSubmit() throws Exception
	{
		Submit submit = makeSubmitMockWithRequest();
		desktopPanel.beforeSubmit( submit, mock( SubmitContext.class ) );

		assertThat( desktopPanel.getSubmitButton(), not( is( enabled() ) ) );
		assertThat( containerWalker.findButtonWithIcon( "create_empty_request.gif" ), not( is( enabled() ) ) );
		assertThat( containerWalker.findButtonWithIcon( "clone_request.gif" ), not( is( enabled() ) ) );
	}

	@Test
	public void reenablesInteractionsAfterSubmit() throws Exception
	{
		Submit submit = makeSubmitMockWithRequest();
		SubmitContext submitContext = mock( SubmitContext.class );
		desktopPanel.beforeSubmit( submit, submitContext );
		desktopPanel.afterSubmit( submit, submitContext );

		assertThat( containerWalker.findButtonWithIcon( "create_empty_request.gif" ), is( enabled() ) );
		assertThat( containerWalker.findButtonWithIcon( "clone_request.gif" ), is( enabled() ) );

	}


	private Submit makeSubmitMockWithRequest()
	{
		Submit submit = mock( Submit.class );
		when( submit.getRequest() ).thenReturn( request );
		return submit;
	}


}
