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

package com.eviware.soapui.utils;

import com.eviware.soapui.support.UISupport;
import org.junit.Test;

import static com.eviware.soapui.utils.StubbedDialogs.hasConfirmationWithQuestion;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * Demo code for the StubbedDialogsTest functionality
 */
public class StubbedDialogsTest extends StubbedDialogsTestBase {

    @Test
    public void catchesErrorMessage() throws Exception {
        String errorMessage = "The shit's hit the fan!";

        UISupport.showErrorMessage(errorMessage);
        assertThat(stubbedDialogs.getErrorMessages(), hasItem(errorMessage));
    }

    @Test
    public void catchesInfoMessages() throws Exception {
        String infoMessage = "Some info";

        UISupport.showInfoMessage(infoMessage);
        assertThat(stubbedDialogs.getInfoMessages(), hasItem(infoMessage));
    }

    @Test
    public void catchesConfirmQuestion() {
        String question = "Are you sure?";

        UISupport.confirm(question, "title");
        assertThat(stubbedDialogs.getConfirmations(), hasConfirmationWithQuestion(question));
    }

    @Test
    public void canMockPositiveConfirmResult() {
        stubbedDialogs.mockConfirmWithReturnValue(true);

        boolean reply = UISupport.confirm("", "");
        assertThat(reply, equalTo(true));
    }

    @Test
    public void canMockNegativeConfirmResult() {
        stubbedDialogs.mockConfirmWithReturnValue(false);

        boolean reply = UISupport.confirm("", "");
        assertThat(reply, equalTo(false));
    }

    @Test
    public void canMockNullConfirmResult() {
        stubbedDialogs.mockConfirmWithReturnValue(null);

        Boolean reply = UISupport.confirmOrCancel("", "");
        assertThat(reply, nullValue());
    }

    @Test
    public void canMockMultipleReturnValuesForConfirmation() {
        stubbedDialogs.mockConfirmWithReturnValue(true, false, null);

        assertThat(UISupport.confirmOrCancel("", ""), equalTo(true));
        assertThat(UISupport.confirmOrCancel("", ""), equalTo(false));
        assertThat(UISupport.confirmOrCancel("", ""), nullValue());
    }

    @Test
    public void returnsLastMockedValueIfMoreInvocationsThanValues() {
        stubbedDialogs.mockConfirmWithReturnValue(true, false);

        assertThat(UISupport.confirmOrCancel("", ""), equalTo(true));
        assertThat(UISupport.confirmOrCancel("", ""), equalTo(false));
        assertThat(UISupport.confirmOrCancel("", ""), equalTo(false));
        assertThat(UISupport.confirmOrCancel("", ""), equalTo(false));
    }
}

