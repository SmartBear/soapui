/*
 * Copyright 2004-2014 SmartBear Software
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
package com.smartbear.soapui.other.soap;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.endpoint.DefaultEndpointStrategy;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import com.smartbear.soapui.utils.IntegrationTest;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.JTableFixture;
import org.fest.swing.security.ExitCallHook;
import org.fest.swing.security.NoExitSecurityManagerInstaller;
import org.junit.*;
import org.junit.experimental.categories.Category;

/**
 * FEST-based test verifying parts of the functionality of DefaultEndpointStrategyConfigurationPaneTest.
 */
@Category(IntegrationTest.class)
public class DefaultEndpointStrategyConfigurationPaneTest {
    private static final int WAIT_FOR_LAST_TEST_TO_SHUTDOWN = 3000;
    private Robot robot;
    private RestService restService;

    private com.eviware.soapui.impl.wsdl.endpoint.DefaultEndpointStrategyConfigurationPanel configurationPanel;

    private static NoExitSecurityManagerInstaller noExitSecurityManagerInstaller;

    @BeforeClass
    public static void setUpOnce() {
        System.out.println("Installing jvm exit protection");
        noExitSecurityManagerInstaller = NoExitSecurityManagerInstaller.installNoExitSecurityManager(new ExitCallHook() {
            @Override
            public void exitCalled(int status) {
                System.out.print("Exit status : " + status);
            }
        });
    }

    @AfterClass
    public static void classTearDown() throws InterruptedException {
        Thread.sleep(WAIT_FOR_LAST_TEST_TO_SHUTDOWN);
        System.out.println("Shuting down jvm exit protection");
        noExitSecurityManagerInstaller.uninstall();
    }

    @Before
    public void setUp() throws SoapUIException {
        robot = BasicRobot.robotWithCurrentAwtHierarchy();
        restService = ModelItemFactory.makeRestService();
        DefaultEndpointStrategy strategy = new DefaultEndpointStrategy();
        strategy.init(restService.getProject());
        configurationPanel = new com.eviware.soapui.impl.wsdl.endpoint.DefaultEndpointStrategyConfigurationPanel(restService, strategy);
    }

    @Test
    public void updatesTableWithNewEndpoint() throws Exception {
        JTableFixture tableFixture = new JTableFixture(robot, configurationPanel.table);

        String endpoint = "http://sljll.com";
        restService.addEndpoint(endpoint);
        // this call will fail if the new endpoint is not found in the table
        tableFixture.cell(endpoint);
    }

    @After
    public void tearDown() {
        robot.cleanUp();
    }
}
