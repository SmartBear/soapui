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
package com.smartbear.soapui.cucumber;

import com.smartbear.soapui.utils.IntegrationTest;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.fest.swing.security.ExitCallHook;
import org.fest.swing.security.NoExitSecurityManagerInstaller;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(IntegrationTest.class)
@RunWith(Cucumber.class)
@CucumberOptions(
        glue = "com.smartbear.soapui.stepdefs.fest",
        features = "src/test/resources/features/",
        tags = "@AutomatedWithFest",
        format = "json:target/cucumber-fest-results.json")
public class CucumberFestRunner {
    public static final int WAIT_FOR_LAST_TEST_TO_SHUTDOWN = 3000;
    private static NoExitSecurityManagerInstaller noExitSecurityManagerInstaller;

    @BeforeClass
    public static void setUp() {
        System.out.println("Installing jvm exit protection");
        noExitSecurityManagerInstaller = NoExitSecurityManagerInstaller.installNoExitSecurityManager(new ExitCallHook() {
            @Override
            public void exitCalled(int status) {
                System.out.println("Exit status : " + status);
            }
        });
    }

    @AfterClass
    public static void tearDown() throws InterruptedException {
        // TODO This is needed to ensure that the last test have stopped before uninstalling, we need a more
        // clever way to wait for the test though
        Thread.sleep(WAIT_FOR_LAST_TEST_TO_SHUTDOWN);
        System.out.println("Shuting down jvm exit protection");
        noExitSecurityManagerInstaller.uninstall();
    }
}