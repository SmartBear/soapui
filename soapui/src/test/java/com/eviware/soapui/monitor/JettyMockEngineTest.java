/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
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

package com.eviware.soapui.monitor;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.mock.MockRunContext;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.settings.SSLSettings;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JettyMockEngineTest {

    @Mock
    private MockRunner mockRunner;
    @Mock
    private MockRunContext mockRunContext;
    @Mock
    private MockService mockService;

    // system under test (sut)
    private JettyMockEngine sut;
    private Field addedSslConnectorField;

    @Before
    public void setUp() throws Exception {
        SoapUI.getSettings().setString(SSLSettings.MOCK_PASSWORD, "abc");
        SoapUI.getSettings().setString(SSLSettings.MOCK_KEYSTORE_PASSWORD, "abc");
        SoapUI.getSettings().setBoolean(HttpSettings.LEAVE_MOCKENGINE, false);
        SoapUI.getSettings().setBoolean(SSLSettings.ENABLE_MOCK_SSL, true);

        sut = new JettyMockEngine();
        when(mockRunner.getMockContext()).thenReturn(mockRunContext);
        when(mockRunContext.getMockService()).thenReturn(mockService);
        when(mockService.getPort()).thenReturn(30000);

        addedSslConnectorField = JettyMockEngine.class.getDeclaredField("addedSslConnector");
        addedSslConnectorField.setAccessible(true);
        assertFalse("The sslConnector must not be added before starting the mockService.", (Boolean) addedSslConnectorField.get(sut));
    }

    @Test
    public void restartMockService() throws Exception {
        // Given the mock service with SSL has been started & stopped
        sut.startMockService(mockRunner);
        assertTrue("The sslConnector must be added after starting the mockService!", (Boolean) addedSslConnectorField.get(sut));
        sut.stopMockService(mockRunner);
        assertFalse("The sslConnector must not be added after stopping the mockService!", (Boolean) addedSslConnectorField.get(sut));

        // When
        sut.startMockService(mockRunner);

        // Then
        assertTrue("The sslConnector must be added after restarting the mockService!", (Boolean) addedSslConnectorField.get(sut));
    }
}