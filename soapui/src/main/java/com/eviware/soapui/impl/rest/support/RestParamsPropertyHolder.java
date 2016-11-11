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
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.testsuite.TestProperty;

import java.util.Map;
import java.util.Properties;

public interface RestParamsPropertyHolder extends MutableTestPropertyHolder, Map<String, TestProperty> {

    public RestParamProperty getProperty(String name);

    public void resetValues();

    public int getPropertyIndex(String name);

    public void saveTo(Properties props);

    public RestParamProperty getPropertyAt(int index);

    public PropertyExpansion[] getPropertyExpansions();

    public void setPropertiesLabel(String propertiesLabel);

    public RestParamProperty addProperty(String name);

    public RestParamProperty removeProperty(String propertyName);

    public RestParamProperty get(Object key);

    public void addParameter(RestParamProperty prop);

    void setParameterLocation(RestParamProperty parameter, NewRestResourceActionBase.ParamLocation newLocation);

    /**
     * Internal property class
     *
     * @author ole
     */

    public enum ParameterStyle {
        MATRIX, HEADER, QUERY, TEMPLATE, PLAIN
    }

}
