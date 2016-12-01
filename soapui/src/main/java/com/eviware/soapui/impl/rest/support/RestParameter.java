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

package com.eviware.soapui.impl.rest.support;

import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;

import javax.xml.namespace.QName;

public interface RestParameter {
    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    XmlBeansRestParamsTestPropertyHolder.ParameterStyle getStyle();

    void setStyle(XmlBeansRestParamsTestPropertyHolder.ParameterStyle style);

    NewRestResourceActionBase.ParamLocation getParamLocation();

    void setParamLocation(NewRestResourceActionBase.ParamLocation paramLocation);

    String getValue();

    void setValue(String value);

    boolean isReadOnly();

    String getDefaultValue();

    String[] getOptions();

    boolean getRequired();

    QName getType();

    void setOptions(String[] arg0);

    void setRequired(boolean arg0);

    void setType(QName arg0);

    void setDefaultValue(String default1);

    String getPath();

    void setPath(String path);
}
