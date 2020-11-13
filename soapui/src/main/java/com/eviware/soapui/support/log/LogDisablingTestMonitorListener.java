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

package com.eviware.soapui.support.log;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.settings.UISettings;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.HashSet;
import java.util.Set;

/**
 * Disables httpclient and groovy logs during loadtests and securitytests
 *
 * @author ole
 */

public final class LogDisablingTestMonitorListener extends TestMonitorListenerAdapter {
    private Set<LoadTestRunner> loadTestRunners = new HashSet<LoadTestRunner>();
    private Set<SecurityTestRunner> securityTestRunners = new HashSet<SecurityTestRunner>();

    public void loadTestStarted(LoadTestRunner runner) {
        if (loadTestRunners.isEmpty()) {
            LogManager.getLogger(SoapUI.class).info("Disabling logs during loadtests");
            Configurator.setLevel("org.apache.http.wire", Level.OFF);

            if (!SoapUI.getSettings().getBoolean(UISettings.DONT_DISABLE_GROOVY_LOG)) {
                Configurator.setLevel("groovy.log", Level.OFF);
            }
        }

        loadTestRunners.add(runner);
    }

    public void loadTestFinished(LoadTestRunner runner) {
        loadTestRunners.remove(runner);

        if (loadTestRunners.isEmpty()) {
            Configurator.setLevel("org.apache.http.wire", Level.DEBUG);
            Configurator.setLevel("groovy.log", Level.DEBUG);
            LogManager.getLogger(SoapUI.class).info("Enabled logs after loadtests");
        }
    }

    public void securityTestStarted(SecurityTestRunner runner) {
        if (securityTestRunners.isEmpty()) {
            // Logger.getLogger( SoapUI.class ).info(
            // "Disabling logs during securitytests" );
            // Logger.getLogger( "org.apache.http.wire" ).setLevel( Level.OFF );

            // if( !SoapUI.getSettings().getBoolean(
            // UISettings.DONT_DISABLE_GROOVY_LOG ) )
            // Logger.getLogger( "groovy.log" ).setLevel( Level.OFF );
        }

        securityTestRunners.add(runner);
    }

    public void securityTestFinished(SecurityTestRunner runner) {
        securityTestRunners.remove(runner);

        if (securityTestRunners.isEmpty()) {
            // Logger.getLogger( "org.apache.http.wire" ).setLevel( Level.DEBUG );
            // Logger.getLogger( "groovy.log" ).setLevel( Level.DEBUG );
            // Logger.getLogger( SoapUI.class ).info(
            // "Enabled logs after securitytests" );
        }
    }
}
