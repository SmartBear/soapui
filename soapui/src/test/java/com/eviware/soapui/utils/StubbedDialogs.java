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

import com.eviware.soapui.support.NullProgressDialog;
import com.eviware.x.dialogs.XDialogs;
import com.eviware.x.dialogs.XProgressDialog;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A stub of the Dialogs class, to be used in unit testing of GUIs.
 */
public class StubbedDialogs implements XDialogs {

    private List<String> errorMessages = new ArrayList<String>();
    private List<String> infoMessages = new ArrayList<String>();
    private List<Prompt> prompts = new ArrayList<Prompt>();
    private List<Confirmation> confirmations = new ArrayList<Confirmation>();
    private boolean mockingPromptValue = false;
    private Object valueToReturnFromPrompt = null;
    private boolean mockingConfirmValue;
    private List<Boolean> valuesToReturnFromConfirm = new ArrayList<Boolean>();
    private int currentValueToReturnFromConfirm = 0;

    @Override
    public void showErrorMessage(String message) {
        errorMessages.add(message);
    }

    @Override
    public void showInfoMessage(String message) {
        infoMessages.add(message);
    }

    @Override
    public void showInfoMessage(String message, String title) {
        infoMessages.add(message);
    }

    @Override
    public void showExtendedInfo(String title, String description, String content, Dimension size) {
        if ("Error".equals(title)) {
            errorMessages.add(content);
        } else {
            infoMessages.add(content);
        }
    }

    @Override
    public boolean confirm(String question, String title) {
        confirmations.add(new Confirmation(question, title));
        if (mockingConfirmValue) {
            Boolean currentConfirmationReturnValue = getCurrentConfirmationReturnValue();
            if (currentConfirmationReturnValue == null) {
                return false;
            }
            return currentConfirmationReturnValue;
        }
        return false;
    }

    @Override
    public boolean confirm(String question, String title, Component parent) {
        return confirm(question, title);
    }

    @Override
    public Boolean confirmOrCancel(String question, String title) {
        confirmations.add(new Confirmation(question, title));
        if (mockingConfirmValue) {
            return getCurrentConfirmationReturnValue();
        }
        return null;
    }

    private Boolean getCurrentConfirmationReturnValue() {
        Boolean returnValue = valuesToReturnFromConfirm.get(currentValueToReturnFromConfirm);
        if (currentValueToReturnFromConfirm < valuesToReturnFromConfirm.size() - 1) {
            currentValueToReturnFromConfirm++;
        }
        return returnValue;
    }

    @Override
    public int yesYesToAllOrNo(String question, String title) {
        return 0;
    }

    @Override
    public String prompt(String question, String title, String value) {
        prompts.add(new Prompt(question, title, value));
        return mockingPromptValue ? (String) valueToReturnFromPrompt : value;
    }

    @Override
    public String prompt(String question, String title) {
        prompts.add(new Prompt(question, title, null));
        return mockingPromptValue ? (String) valueToReturnFromPrompt : null;
    }

    @Override
    public Object prompt(String question, String title, Object[] objects) {
        prompts.add(new Prompt(question, title, objects));
        return mockingPromptValue ? valueToReturnFromPrompt : objects[0];
    }

    @Override
    public Object prompt(String question, String title, Object[] objects, String value) {
        prompts.add(new Prompt(question, title, objects));
        return value;
    }

    @Override
    public char[] promptPassword(String question, String title) {
        return new char[0];
    }

    @Override
    public XProgressDialog createProgressDialog(String label, int length, String initialValue, boolean canCancel) {
        return new NullProgressDialog();
    }

    @Override
    public boolean confirmExtendedInfo(String title, String description, String content, Dimension size) {
        return confirm(content, title);
    }

    @Override
    public Boolean confirmOrCancleExtendedInfo(String title, String description, String content, Dimension size) {
        return null;
    }

    @Override
    public String selectXPath(String title, String info, String xml, String xpath) {
        return null;
    }

    @Override
    public String selectJsonPath(String title, String info, String json, String jsonPath) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public List<String> getInfoMessages() {
        return infoMessages;
    }

    public List<Prompt> getPrompts() {
        return prompts;
    }

    public void mockPromptWithReturnValue(Object value) {
        mockingPromptValue = true;
        valueToReturnFromPrompt = value;
    }

    public List<Confirmation> getConfirmations() {
        return confirmations;
    }

    public void mockConfirmWithReturnValue(Boolean value) {
        mockingConfirmValue = true;
        currentValueToReturnFromConfirm = 0;
        valuesToReturnFromConfirm.add(value);
    }

    public void mockConfirmWithReturnValue(Boolean value, Boolean... values) {
        mockConfirmWithReturnValue(value);
        if (values != null) {
            valuesToReturnFromConfirm.addAll(Arrays.asList(values));
        } else {
            valuesToReturnFromConfirm.add(null);
        }
    }

    public static class Prompt {
        public final String question;
        public final String title;
        public final Object value;

        public Prompt(String question, String title, Object value) {
            this.question = question;
            this.title = title;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Prompt{" +
                    "question='" + question + '\'' +
                    ", title='" + title + '\'' +
                    ", value=" + value +
                    '}';
        }
    }

    public static class Confirmation {

        public final String question;
        public final String title;

        public Confirmation(String question, String title) {
            this.question = question;
            this.title = title;
        }

        @Override
        public String toString() {
            return "Confirmation{" +
                    "question='" + question + '\'' +
                    ", title='" + title + '\'' +
                    '}';
        }
    }

    public static Matcher<List<Prompt>> hasPromptWithValue(final String value) {
        return new TypeSafeMatcher<List<Prompt>>() {
            @Override
            public boolean matchesSafely(List<Prompt> prompts) {
                for (Prompt prompt : prompts) {
                    if (prompt.value.equals(value)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a Prompt list with a prompt with the value '" + value + "'");
            }
        };
    }

    public static Matcher<List<Confirmation>> hasConfirmationWithQuestion(final String question) {
        return new TypeSafeMatcher<List<Confirmation>>() {
            @Override
            public boolean matchesSafely(List<Confirmation> confirmations) {
                for (Confirmation confirmation : confirmations) {
                    if (confirmation.question.equals(question)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a Confirm list with a confirm with the question '" + question + "'");
            }
        };
    }

    public static Matcher<List<Confirmation>> hasConfirmationWithQuestionThat(final Matcher<String> stringMatcher) {
        return new TypeSafeMatcher<List<Confirmation>>() {
            @Override
            public boolean matchesSafely(List<Confirmation> confirmations) {
                for (Confirmation confirmation : confirmations) {
                    if (stringMatcher.matches(confirmation.question)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a confirmation with a question that ");
                stringMatcher.describeTo(description);
            }
        };
    }
}
