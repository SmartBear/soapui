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

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractSubmitContext;
import com.eviware.soapui.model.testsuite.TestStep;

/**
 * Default implementation
 */

public class WsdlSubmitContext extends AbstractSubmitContext {
    private final TestStep step;

    public WsdlSubmitContext(ModelItem context) {
        super(context);
        step = context instanceof TestStep ? (TestStep) context : null;
    }

    public Object getProperty(String name) {
        return getProperty(name, step, (WsdlTestCase) (step == null ? null : step.getTestCase()));
    }

    @Override
    public Object get(Object key) {
        if ("settings".equals(key)) {
            return getSettings();
        }

        return getProperty(key.toString());
    }

    @Override
    public Object put(String key, Object value) {
        Object oldValue = get(key);
        setProperty(key, value);
        return oldValue;
    }

    public Settings getSettings() {
        return step != null ? step.getSettings() : null;
    }

    public String expand(String content) {
        return PropertyExpander.expandProperties(this, content);
    }
}
