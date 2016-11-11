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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

public class BeanPathPropertySupport extends AbstractPathPropertySupport {
    private Object config;

    public BeanPathPropertySupport(AbstractWsdlModelItem<?> modelItem, String propertyName) {
        this(modelItem, modelItem.getConfig(), propertyName);
    }

    public BeanPathPropertySupport(AbstractWsdlModelItem<?> modelItem, Object config, String propertyName) {
        super(modelItem, propertyName);
        this.config = config;
    }

    public void setPropertyValue(String value) throws IllegalAccessException, InvocationTargetException {
        BeanUtils.setProperty(config, getPropertyName(), value);
    }

    public String getPropertyValue() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return BeanUtils.getProperty(config, getPropertyName());
    }

    public void setConfig(Object config) {
        this.config = config;
    }
}
