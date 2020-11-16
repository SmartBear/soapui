/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.impl.wsdl.panels.request;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.submit.RequestTransport;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.utils.ContainerWalker;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.AbstractButton;

import static com.eviware.soapui.utils.SwingMatchers.enabled;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for WsdlRequestDesktopPanel, indirectly testing AbstractHttpRequestDesktopPanel as well.
 */
public class WsdlRequestDesktopPanelTest {

    public static final String ENDPOINT_URL = "http://google.com/webservices";
    private WsdlRequestDesktopPanel desktopPanel;
    private WsdlRequest request;
    private ContainerWalker containerWalker;

    @BeforeClass
    public static void initSoapUICoreLog() {
        DefaultSoapUICore.log = mock(Logger.class);
    }

    @Before
    public void setUp() throws Exception {
        request = mock(WsdlRequest.class);
        when(request.getEndpoint()).thenReturn(ENDPOINT_URL);
        WsdlProject stubbedProject = mock(WsdlProject.class);
        when(stubbedProject.isEnvironmentMode()).thenReturn(true);
        when(request.getParent()).thenReturn(stubbedProject);
        XmlBeansSettingsImpl settings = mock(XmlBeansSettingsImpl.class);
        when(request.getSettings()).thenReturn(settings);
        desktopPanel = new WsdlRequestDesktopPanel(request);
        containerWalker = new ContainerWalker(desktopPanel);
    }

    @Test
    public void returnsCorrectHelpUrl() throws Exception {
        assertThat(desktopPanel.getHelpUrl(), is(HelpUrls.REQUESTEDITOR_HELP_URL));
    }

    @Ignore
    @Test
    public void endpointComboBoxIsEditable() throws Exception {
        assertThat(containerWalker.findComboBoxWithValue(request).isEditable(), is(false));
    }

    @Test
    public void returnsRequest() throws Exception {
        assertThat(desktopPanel.getRequest(), is(request));
    }

    @Test
    public void submitsRequestWhenRunButtonIsClicked() throws Exception {
        RequestTransport anyTransport = mock(RequestTransport.class);
        WsdlSubmit<WsdlRequest> submit = new WsdlSubmit<WsdlRequest>(request, new SubmitListener[0], anyTransport);
        when(request.submit(isA(SubmitContext.class), eq(true))).thenReturn(submit);

        AbstractButton runButton = desktopPanel.getSubmitButton();
        runButton.doClick();
        verify(request).submit(isA(SubmitContext.class), eq(true));
    }

    @Ignore
    @Test
    public void disablesSubmitButtonWhenEndpointIsEmpty() throws Exception {
        //containerWalker.findComboBoxWithValue( ENDPOINT_URL ).getModel().se

        assertThat(desktopPanel.getSubmitButton(), is(not(enabled())));
    }

    @Test
    public void disablesInteractionsDuringSubmit() throws Exception {
        Submit submit = makeSubmitMockWithRequest();
        desktopPanel.beforeSubmit(submit, mock(SubmitContext.class));

        assertThat(desktopPanel.getSubmitButton(), is(not(enabled())));
        assertThat(containerWalker.findButtonWithIcon("create_empty_request.gif"), is(not(enabled())));
        assertThat(containerWalker.findButtonWithIcon("clone.png"), is(not(enabled())));
    }

    @Test
    public void reenablesInteractionsAfterSubmit() throws Exception {
        Submit submit = makeSubmitMockWithRequest();
        SubmitContext submitContext = mock(SubmitContext.class);
        desktopPanel.beforeSubmit(submit, submitContext);
        desktopPanel.afterSubmit(submit, submitContext);

        assertThat(containerWalker.findButtonWithIcon("create_empty_request.gif"), is(enabled()));
        assertThat(containerWalker.findButtonWithIcon("clone.png"), is(enabled()));

    }


	/* Helpers */

    private Submit makeSubmitMockWithRequest() {
        Submit submit = mock(Submit.class);
        when(submit.getRequest()).thenReturn(request);
        return submit;
    }


}
