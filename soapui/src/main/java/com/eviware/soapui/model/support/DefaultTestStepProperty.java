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

package com.eviware.soapui.model.support;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.RenameableTestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlString;

import javax.xml.namespace.QName;

/**
 * Default implementation of TestStepProperty interface
 *
 * @author Ole.Matzura
 */

public class DefaultTestStepProperty implements TestStepProperty, RenameableTestProperty {
    private String name;
    private boolean isReadOnly;
    private String description;
    private PropertyHandler handler;
    private final WsdlTestStep testStep;
    private boolean requestPart;

    public DefaultTestStepProperty(String name, boolean isReadOnly, PropertyHandler handler, WsdlTestStep testStep) {
        this.name = name;
        this.isReadOnly = isReadOnly;
        this.handler = handler;
        this.testStep = testStep;
    }

    public DefaultTestStepProperty(String name, boolean isReadOnly, PropertyHandler handler, WsdlTestStep testStep,
                                   boolean requestPart) {
        this.name = name;
        this.isReadOnly = isReadOnly;
        this.handler = handler;
        this.testStep = testStep;
        this.requestPart = requestPart;
    }

    public DefaultTestStepProperty(String name, WsdlTestStep testStep) {
        this(name, false, new SimplePropertyHandler(), testStep);
    }

    public DefaultTestStepProperty(String name, boolean isReadOnly, WsdlTestStep testStep) {
        this(name, isReadOnly, new SimplePropertyHandler(), testStep);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIsReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setPropertyHandler(PropertyHandler handler) {
        this.handler = handler;
    }

    public String getValue() {
        return handler == null ? null : handler.getValue(this);
    }

    public void setValue(String value) {
        if (isReadOnly()) {
            throw new RuntimeException("Trying to set read-only property [" + getName() + "]");
        }

        if (handler != null) {
            handler.setValue(this, value);
        }
    }

    public TestStep getTestStep() {
        return testStep;
    }

    /**
     * Handler for providing and setting property values
     *
     * @author Ole.Matzura
     */

    public interface PropertyHandler {
        public String getValue(DefaultTestStepProperty property);

        public void setValue(DefaultTestStepProperty property, String value);
    }

    /**
     * Empty implementation of PropertyHandler interface
     *
     * @author Ole.Matzura
     */

    public static class PropertyHandlerAdapter implements PropertyHandler {
        public String getValue(DefaultTestStepProperty property) {
            return null;
        }

        public void setValue(DefaultTestStepProperty property, String value) {
        }
    }

    /**
     * Simple implementation of PropertyHandler interface
     *
     * @author Ole.Matzura
     */

    public static class SimplePropertyHandler implements PropertyHandler {
        private String value;

        public String getValue(DefaultTestStepProperty property) {
            return value;
        }

        public void setValue(DefaultTestStepProperty property, String value) {
            this.value = value;
        }
    }

    public QName getType() {
        return XmlString.type.getName();
    }

    public ModelItem getModelItem() {
        return testStep;
    }

    public String getDefaultValue() {
        return null;
    }

    public boolean isRequestPart() {
        return requestPart;
    }

    @Override
    public SchemaType getSchemaType() {
        return XmlString.type;
    }

}
