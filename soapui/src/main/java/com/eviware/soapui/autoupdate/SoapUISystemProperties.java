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

package com.eviware.soapui.autoupdate;

/**
 * Created by avdeev on 19.08.2014.
 */
public interface SoapUISystemProperties {
    public final static String VERSION = "soapui.version";
    public final static String SOAP_UI_UPDATE_URL = "http://dl.eviware.com/version-update/soapui-updates-os.xml";//http://resources.ej-technologies.com/install4j/help/doc/indexRedirect.html?http&&&resources.ej-technologies.com/install4j/help/doc/steps/installerGui/autoUpdateOptions.html
    public final static String SOAP_UI_UPDATE_ADDITIONAL_URL = "http://dl.eviware.com/version-update/soapui-updates-additional.xml";
    public final static String SOAP_UI_UPDATER_APP_ID = "4969";
}
