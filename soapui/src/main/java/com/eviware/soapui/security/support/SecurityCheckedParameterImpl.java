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

package com.eviware.soapui.security.support;

import com.eviware.soapui.config.CheckedParameterConfig;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import org.apache.xmlbeans.SchemaType;

/**
 * ... holds information on parameter which is excluded from request and
 * security test is applied on.
 *
 * @author robert
 */
public class SecurityCheckedParameterImpl implements SecurityCheckedParameter {

    private CheckedParameterConfig config;

    public SecurityCheckedParameterImpl(CheckedParameterConfig param) {
        this.config = param;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.security.support.SecurityCheckedParameter#getName()
     */
    public String getName() {
        return config.getParameterName();
    }

    /**
     * @param name parameter name
     */
    public void setName(String name) {
        config.setParameterName(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.security.support.SecurityCheckedParameter#getXPath()
     */
    public String getXpath() {
        return config.getXpath();
    }

    /**
     * @param xpath parameter XPath
     */
    public void setXpath(String xpath) {
        config.setXpath(xpath);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.security.support.SecurityCheckedParameter#getType()
     */
    public String getType() {
        return config.getType();
    }

    /**
     * @param schemaType parameter xml type
     */
    public void setType(SchemaType schemaType) {
        config.setType(schemaType.toString());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.security.support.SecurityCheckedParameter#isChecked()
     */
    public boolean isChecked() {
        return config.getChecked();
    }

    /**
     * Enable/dissable using this parameter in security check..
     *
     * @param checked
     */
    public void setChecked(boolean checked) {
        config.setChecked(checked);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.security.support.SecurityCheckedParameter#getLabel()
     */
    public String getLabel() {
        return config.getLabel();
    }

    /**
     * @param label parameter label
     */
    public void setLabel(String label) {
        config.setLabel(label);
    }

    /**
     * @param config parameter config
     */
    public void setConfig(CheckedParameterConfig config) {
        this.config = config;
    }
}
