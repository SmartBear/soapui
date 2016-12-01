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

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.support.http.HttpRequestTestStep;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.support.resolver.ResolveContext;

import java.beans.PropertyChangeListener;

public interface HttpTestRequestStepInterface extends PropertyChangeListener, PropertyExpansionContainer, Assertable,
        HttpRequestTestStep, ModelItem {
    public WsdlTestStep clone(WsdlTestCase targetTestCase, String name);

    public void release();

    public void resetConfigOnMove(TestStepConfig config);

    public HttpTestRequestInterface<?> getTestRequest();

    public void setName(String name);

    public boolean dependsOn(AbstractWsdlModelItem<?> modelItem);

    public void beforeSave();

    public void setDescription(String description);

    public String getDefaultSourcePropertyName();

    public String getDefaultTargetPropertyName();

    public void resolve(ResolveContext<?> context);

    public WsdlTestCase getTestCase();
}
