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

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.OAuth2ProfileContainerConfig;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;

import java.util.ArrayList;
import java.util.List;

public interface OAuth2ProfileContainer extends PropertyExpansionContainer {

    public WsdlProject getProject();

    public List<OAuth2Profile> getOAuth2ProfileList();

    public void release();

    public OAuth2ProfileContainerConfig getConfig();

    public OAuth2Profile addNewOAuth2Profile(String profileName);

    public void removeProfile(String profileName);

    ArrayList<String> getOAuth2ProfileNameList();

    OAuth2Profile getProfileByName(String profileName);

    void addOAuth2ProfileListener(OAuth2ProfileListener listener);

    void removeOAuth2ProfileListener(OAuth2ProfileListener listener);

    void renameProfile(String profileOldName, String newName);
}
