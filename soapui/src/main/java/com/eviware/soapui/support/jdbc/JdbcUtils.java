/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
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

package com.eviware.soapui.support.jdbc;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.GroovyUtils;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Matcher;

public class JdbcUtils {

    public static final String PASS_TEMPLATE = "PASS_VALUE";

    public static Connection initConnection(PropertyExpansionContext context, String driver, String connectionString,
                                            String password) throws SQLException, SoapUIException {
        if (JdbcUtils.missingConnSettings(driver, connectionString)) {
            throw new SoapUIException("Some connections settings are missing");
        }
        String drvr = PropertyExpander.expandProperties(context, driver).trim();
        String connStr = PropertyExpander.expandProperties(context, connectionString).trim();
        String pass = StringUtils.hasContent(password) ? PropertyExpander.expandProperties(context, password).trim()
                : "";
        String masskedPass = connStr.replace(PASS_TEMPLATE, "#####");
        if (connStr.contains(PASS_TEMPLATE)) {
            pass = Matcher.quoteReplacement(pass);
            connStr = connStr.replaceFirst(PASS_TEMPLATE, pass);
        }
        try {
            GroovyUtils.registerJdbcDriver(drvr);
            DriverManager.getDriver(connStr);
        } catch (SQLException e) {
            // SoapUI.logError( e );
            try {
                Class.forName(drvr).newInstance();
            } catch (Exception e1) {
                SoapUI.logError(e);
                throw new SoapUIException("Failed to init connection for driver [" + drvr + "], connectionString ["
                        + masskedPass + "]");
            }
        }
        return DriverManager.getConnection(connStr);

    }

    public static boolean hasMasskedPass(String connStr) {
        return !StringUtils.isNullOrEmpty(connStr) ? connStr.contains(PASS_TEMPLATE) : false;
    }

    public static boolean missingConnSettings(String driver, String connectionString) {
        return StringUtils.isNullOrEmpty(driver) || StringUtils.isNullOrEmpty(connectionString);
    }

}
