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

package com.eviware.soapui.settings;

/**
 * Tools/Integration-related settings constants
 *
 * @author Ole.Matzura
 */

public interface LoadUISettings {
    public final static String LOADUI_PATH = LoadUISettings.class.getSimpleName() + "@" + "loadui_path";
    public final static String LOADUI_CAJO_SERVER = LoadUISettings.class.getSimpleName() + "@" + "cajo_server_name";
    public final static String LOADUI_CAJO_PORT = LoadUISettings.class.getSimpleName() + "@" + "cajo_port";
    public final static String LOADUI_CAJO_ITEM_NAME = LoadUISettings.class.getSimpleName() + "@" + "cajo_item_name";
    public final static String SOAPUI_CAJO_PORT = LoadUISettings.class.getSimpleName() + "@" + "cajo_soapui_port";

    public final static String START_CAJO_SERVER_AT_STARTUP = LoadUISettings.class.getSimpleName() + "@" + "start_cajo_server_at_startup";

}
