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

package com.eviware.soapui.support.propertyexpansion;

import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestInterface;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.scripting.SoapUIScriptGenerator;

public abstract class AbstractPropertyExpansionTarget implements PropertyExpansionTarget {
    private ModelItem modelItem;

    public AbstractPropertyExpansionTarget(ModelItem modelItem) {
        this.modelItem = modelItem;
    }

    public ModelItem getContextModelItem() {
        if (modelItem instanceof WsdlTestRequest) {
            modelItem = ((WsdlTestRequest) modelItem).getTestStep();
        } else if (modelItem instanceof HttpTestRequestInterface<?>) {
            modelItem = ((HttpTestRequestInterface<?>) modelItem).getTestStep();
        }

        return modelItem;
    }

    public ModelItem getModelItem() {
        return modelItem;
    }

    protected String createContextExpansion(String name, PropertyExpansion expansion) {
        SoapUIScriptGenerator scriptGenerator = SoapUIScriptEngineRegistry.createScriptGenerator(getModelItem());
        return scriptGenerator.createContextExpansion(name, expansion);
    }
}
