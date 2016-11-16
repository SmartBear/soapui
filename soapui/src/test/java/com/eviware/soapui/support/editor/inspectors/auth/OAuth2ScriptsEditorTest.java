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

package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.actions.oauth.BrowserListener;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2Parameters;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2TokenExtractor;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ContainerWalker;
import com.eviware.soapui.utils.StubbedDialogsTestBase;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.junit.Before;
import org.junit.Test;

import javax.swing.AbstractButton;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.eviware.soapui.utils.CommonMatchers.aCollectionWithSize;
import static com.eviware.soapui.utils.CommonMatchers.anEmptyCollection;
import static com.eviware.soapui.utils.ModelItemFactory.makeOAuth2Profile;
import static com.eviware.soapui.utils.SwingMatchers.enabled;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 *
 */
public class OAuth2ScriptsEditorTest extends StubbedDialogsTestBase {

    private OAuth2ScriptsEditor editor;
    private ContainerWalker containerWalker;
    private OAuth2ScriptsEditorTest.StubbedExtractor stubbedExtractor;
    private OAuth2Profile oAuth2Profile;

    @Before
    public void setUp() throws Exception {
        oAuth2Profile = createProfileWith(Collections.<String>emptyList());
        editor = new TestableOAuth2ScriptsEditor(oAuth2Profile);
        containerWalker = new ContainerWalker(editor);
        stubbedExtractor = new StubbedExtractor();
    }

    @Test
    public void canBeInitializedWithoutExistingScripts() throws Exception {
        assertThat(editor.getJavaScripts(), is(Arrays.asList("", "")));
    }

    @Test
    public void getsJavaScriptsEnteredByUser() throws Exception {
        final String firstScript = "alert('first')";
        final String secondScript = "alert('second')";

        enterScript(0, firstScript);
        enterScript(1, secondScript);

        assertThat(editor.getJavaScripts(), is(Arrays.asList(firstScript, secondScript)));
    }

    @Test
    public void getsJavaScriptsFromInitialization() throws Exception {
        List<String> scripts = Arrays.asList("alert('hello')", "window.status='hello'");
        OAuth2ScriptsEditor editorWithExistingScripts = new OAuth2ScriptsEditor(createProfileWith(scripts));
        assertThat(editorWithExistingScripts.getJavaScripts(), is(scripts));
    }

    @Test
    public void showsErrorMessageWhenInvalidScriptIsTested() throws Exception {
        final String invalidScript = "this is clearly invalid";

        containerWalker.findTextComponent(OAuth2ScriptsEditor.DEFAULT_SCRIPT_NAMES[0]).setText(invalidScript);
        containerWalker.findButtonWithName(OAuth2ScriptsEditor.TEST_SCRIPTS_BUTTON_NAME).doClick();

        List<String> errorMessages = stubbedDialogs.getErrorMessages();
        assertThat(errorMessages, is(aCollectionWithSize(1)));
        assertThat(errorMessages.get(0), containsString(invalidScript));
    }

    @Test
    public void showsErrorMessageWhenValidScriptFailsToRun() throws Exception {
        final String validScript = "alert('valid')";

        stubbedExtractor.shouldSimulateJavaScriptErrors = true;
        containerWalker.findTextComponent(OAuth2ScriptsEditor.DEFAULT_SCRIPT_NAMES[0]).setText(validScript);
        containerWalker.findButtonWithName(OAuth2ScriptsEditor.TEST_SCRIPTS_BUTTON_NAME).doClick();
        waitForSwingThread();

        assertThat(stubbedDialogs.getErrorMessages(), is(aCollectionWithSize(1)));
        assertThat(stubbedDialogs.getInfoMessages(), is(anEmptyCollection()));
    }

    @Test
    public void showsInfoMessageButNoErrorMessageWhenValidScriptIsTested() throws Exception {
        final String validScript = "alert('hej')";
        enterScript(0, validScript);
        containerWalker.findButtonWithName(OAuth2ScriptsEditor.TEST_SCRIPTS_BUTTON_NAME).doClick();
        waitForSwingThread();

        assertThat(stubbedDialogs.getErrorMessages(), is(anEmptyCollection()));
        assertThat(stubbedDialogs.getInfoMessages(), is(aCollectionWithSize(1)));
    }

    private void enterScript(int index, String validScript) {
        String scriptName = OAuth2ScriptsEditor.DEFAULT_SCRIPT_NAMES[index];

        containerWalker.findTextComponent(scriptName).setText(validScript);
    }

    @Test
    public void canAddScriptToEditor() throws Exception {
        final String newScript = "callSomeFunction();";
        containerWalker.findButtonWithName(OAuth2ScriptsEditor.ADD_SCRIPT_BUTTON_NAME).doClick();
        containerWalker.rebuildIndex();
        containerWalker.findTextComponent("Page 3").setText(newScript);

        List<String> javaScripts = editor.getJavaScripts();
        assertThat(javaScripts, is(aCollectionWithSize(3)));
        assertThat(javaScripts.get(2), is(newScript));
    }

    @Test
    public void updatesModelWhenAddingScript() throws Exception {
        final String newScript = "callSomeFunction();";
        containerWalker.findButtonWithName(OAuth2ScriptsEditor.ADD_SCRIPT_BUTTON_NAME).doClick();
        containerWalker.rebuildIndex();
        containerWalker.findTextComponent("Page 3").setText(newScript);

        List<String> javaScripts = oAuth2Profile.getAutomationJavaScripts();
        assertThat(javaScripts, is(aCollectionWithSize(3)));
        assertThat(javaScripts.get(2), is(newScript));
    }

    @Test
    public void canRemoveScriptFromEditor() throws Exception {
        removeInputPanel("Input panel 2");

        List<String> javaScripts = editor.getJavaScripts();
        assertThat(javaScripts, is(aCollectionWithSize(1)));
    }

    @Test
    public void scriptRemovalRemovesSelectedScriptFromProfile() throws Exception {
        final String scriptToKeep = "first()";
        final String scriptToDiscard = "second()";
        oAuth2Profile.setAutomationJavaScripts(Arrays.asList(scriptToKeep, scriptToDiscard));
        editor = new TestableOAuth2ScriptsEditor(oAuth2Profile);
        containerWalker = new ContainerWalker(editor);
        removeInputPanel("Input panel 2");

        assertThat(oAuth2Profile.getAutomationJavaScripts(), hasItem(scriptToKeep));
        assertThat(oAuth2Profile.getAutomationJavaScripts(), not(hasItem(scriptToDiscard)));
    }

    private void removeInputPanel(String inputPanelName) {
        editor.selectField(containerWalker.findComponent(inputPanelName, OAuth2ScriptsEditor.InputPanel.class));
        stubbedDialogs.mockConfirmWithReturnValue(true);
        containerWalker.findButtonWithName(OAuth2ScriptsEditor.REMOVE_SCRIPT_BUTTON_NAME).doClick();
    }

    @Test
    public void disablesRemoveButtonAfterRemoval() throws Exception {
        editor.selectField(containerWalker.findComponent("Input panel 2", OAuth2ScriptsEditor.InputPanel.class));
        stubbedDialogs.mockConfirmWithReturnValue(true);
        AbstractButton removeButton = containerWalker.findButtonWithName(OAuth2ScriptsEditor.REMOVE_SCRIPT_BUTTON_NAME);
        removeButton.doClick();

        assertThat(removeButton, is(not(enabled())));
    }

    @Test
    public void doesNotRemoveScriptFromEditorIfActionIsCancelled() throws Exception {
        editor.selectField(containerWalker.findComponent("Input panel 2", OAuth2ScriptsEditor.InputPanel.class));
        // this simulates that the user doesn't confirm the script removal
        stubbedDialogs.mockConfirmWithReturnValue(false);
        containerWalker.findButtonWithName(OAuth2ScriptsEditor.REMOVE_SCRIPT_BUTTON_NAME).doClick();

        List<String> javaScripts = editor.getJavaScripts();
        assertThat(javaScripts, is(aCollectionWithSize(2)));
    }

    @Test
    public void removeButtonEnabledOnlyWhenPanelIsSelected() throws Exception {
        assertThat(containerWalker.findButtonWithName(OAuth2ScriptsEditor.REMOVE_SCRIPT_BUTTON_NAME),
                is(not(enabled())));
        editor.selectField(containerWalker.findComponent("Input panel 2", OAuth2ScriptsEditor.InputPanel.class));
        assertThat(containerWalker.findButtonWithName(OAuth2ScriptsEditor.REMOVE_SCRIPT_BUTTON_NAME),
                is(enabled()));

    }

	/* Helpers */

    private OAuth2Profile createProfileWith(List<String> scripts) throws SoapUIException {
        OAuth2Profile profile = makeOAuth2Profile();
        profile.setAutomationJavaScripts(scripts);
        return profile;
    }

    private void waitForSwingThread() throws InterruptedException {
        Thread.sleep(100);
    }

    private class TestableOAuth2ScriptsEditor extends OAuth2ScriptsEditor {
        public TestableOAuth2ScriptsEditor(OAuth2Profile profile) {
            super(profile);
        }

        @Override
        protected OAuth2TokenExtractor getExtractor() {
            return stubbedExtractor;
        }

    }

    private class StubbedExtractor extends OAuth2TokenExtractor {
        boolean shouldSimulateJavaScriptErrors = false;

        @Override
        public void extractAccessToken(OAuth2Parameters parameters) throws URISyntaxException, MalformedURLException, OAuthSystemException {
            simulateBrowserInteraction();
        }

        private void simulateBrowserInteraction() {
            if (shouldSimulateJavaScriptErrors) {
                for (BrowserListener browserListener : browserListeners) {
                    browserListener.javaScriptExecuted("document.usr.value = 'kalle'",
                            "http://mock.com", new RuntimeException("Mock error"));
                }
            }
            for (BrowserListener browserListener : browserListeners) {
                browserListener.browserClosed();
            }
        }
    }
}
