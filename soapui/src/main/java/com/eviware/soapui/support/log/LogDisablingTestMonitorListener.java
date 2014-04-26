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

package com.eviware.soapui.support.log;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.settings.UISettings;

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
            Logger.getLogger(SoapUI.class).info("Disabling logs during loadtests");
            Logger.getLogger("org.apache.http.wire").setLevel(Level.OFF);

            if (!SoapUI.getSettings().getBoolean(UISettings.DONT_DISABLE_GROOVY_LOG)) {
                Logger.getLogger("groovy.log").setLevel(Level.OFF);
            }
        }

        loadTestRunners.add(runner);
    }

    public void loadTestFinished(LoadTestRunner runner) {
        loadTestRunners.remove(runner);

        if (loadTestRunners.isEmpty()) {
            Logger.getLogger("org.apache.http.wire").setLevel(Level.DEBUG);
            Logger.getLogger("groovy.log").setLevel(Level.DEBUG);
            Logger.getLogger(SoapUI.class).info("Enabled logs after loadtests");
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
