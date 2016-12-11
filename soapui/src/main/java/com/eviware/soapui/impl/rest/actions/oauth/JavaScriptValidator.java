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

package com.eviware.soapui.impl.rest.actions.oauth;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;

/**
 * Simple Javascript syntax validator.
 */
public class JavaScriptValidator {
    public JavaScriptValidationError validate(String script) {
        Context mozillaJavaScriptContext = Context.enter();
        try {
            mozillaJavaScriptContext.compileString(script, "scriptToValidate", 1, null);
            return null;
        } catch (EvaluatorException e) {
            return new JavaScriptValidationError(e.getMessage(), e.lineNumber());
        }
    }

}
