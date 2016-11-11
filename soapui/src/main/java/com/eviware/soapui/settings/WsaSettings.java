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

import com.eviware.soapui.settings.Setting.SettingType;

/**
 * WS-A Settings constants
 *
 * @author dragica.soldo
 */
public interface WsaSettings {
    @Setting(name = "Soap action overrides wsa action", description = "Soap action if present overrides wsa action", type = SettingType.BOOLEAN)
    public final static String SOAP_ACTION_OVERRIDES_WSA_ACTION = WsaSettings.class.getSimpleName() + "@"
            + "soapActionOverridesWsaAction";

    @Setting(name = "Use default RelationshipType", description = "Use default 'reply' for RelationshipType", type = SettingType.BOOLEAN)
    public final static String USE_DEFAULT_RELATIONSHIP_TYPE = WsaSettings.class.getSimpleName() + "@"
            + "useDefaultRelationshipType";

    @Setting(name = "Use default RelatesTo", description = "Use default 'unspecified' for RelatesTo", type = SettingType.BOOLEAN)
    public final static String USE_DEFAULT_RELATES_TO = WsaSettings.class.getSimpleName() + "@" + "useDefaultRelatesTo";

    @Setting(name = "Override existing headers", description = "Replaces existing WS-A headers (or skips them if unchecked) ", type = SettingType.BOOLEAN)
    public final static String OVERRIDE_EXISTING_HEADERS = WsaSettings.class.getSimpleName() + "@"
            + "overrideExistingHeaders";

    @Setting(name = "Enable for optional Addressing policy", description = "Enables WS-Addressing for Addressing Optional='true'", type = SettingType.BOOLEAN)
    public final static String ENABLE_FOR_OPTIONAL = WsaSettings.class.getSimpleName() + "@" + "enableForOptional";

}
