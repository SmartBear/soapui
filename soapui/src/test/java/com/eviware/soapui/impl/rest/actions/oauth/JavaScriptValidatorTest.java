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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class JavaScriptValidatorTest {
    private static final String[] VALID_JAVASCRIPTS = {
            "document.getElementById('approveButton').click()",
            "document.forms[0].submit()",
            "document.getElementById('userNameField').value = 'my.user'",
            "/* a comment */ document.getElementById('userNameField').value = 'my.user'",
            "//a comment\n document.getElementById('userNameField').value = 'my.user'",
            "document.getElementById('usr').value = 'my.user';" +
                    "document.getElementById('pass').pass = 'thePassword';" +
                    "document.getElementById('login_form').submit()"
    };

    private final JavaScriptValidator javaScriptValidator = new JavaScriptValidator();

    @Test
    public void detectsBrokenJavaScript() throws Exception {
        assertThat(javaScriptValidator.validate("this is not valid JavaScript"), is(not(nullValue())));
    }

    @Test
    public void providesCorrectLineNumberForError() throws Exception {
        String threeLineScript = "alert(1)\nalert(2)\nthis is not valid JavaScript";
        JavaScriptValidationError validationError = javaScriptValidator.validate(threeLineScript);
        assertThat(validationError.getLineNumber(), is(3));
    }

    @Test
    public void doesNotComplainAboutSyntacticallyCorrectScripts() throws Exception {
        for (String validJavascript : VALID_JAVASCRIPTS) {
            assertThat(javaScriptValidator.validate(validJavascript), is(nullValue()));
        }
    }
}
