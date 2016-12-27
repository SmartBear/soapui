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

package com.eviware.soapui.model.propertyexpansion.resolvers;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;

public class SubmitPropertyResolver implements PropertyResolver {
    public String resolveProperty(PropertyExpansionContext context, String propertyName, boolean globalOverride) {
        if (propertyName.charAt(0) == PropertyExpansion.SCOPE_PREFIX
                && context.getModelItem() instanceof AbstractHttpRequestInterface<?>) {
            return ResolverUtils.checkForExplicitReference(propertyName, PropertyExpansion.PROJECT_REFERENCE,
                    ((AbstractHttpRequest<?>) context.getModelItem()).getOperation().getInterface().getProject(),
                    context, globalOverride);
        }

        return null;
    }

}
