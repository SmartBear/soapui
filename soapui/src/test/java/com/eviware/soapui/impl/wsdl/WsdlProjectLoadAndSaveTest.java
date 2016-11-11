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

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.project.SaveStatus;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.StubbedDialogsTestBase;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.XmlException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WsdlProjectLoadAndSaveTest extends StubbedDialogsTestBase {
    private static final String PROJECT_NAME = "Project";
    private static final File TEMPORARY_FOLDER = Files.createTempDir();
    private static final String SAMPLE_PROJECT_RELATIVE_PATH = "/sample-soapui-project.xml";
    private static final String SAMPLE_PROJECT_ABSOLUTE_PATH
            = WsdlProjectLoadAndSaveTest.class.getResource(SAMPLE_PROJECT_RELATIVE_PATH).getPath();

    private final InputStream sampleProjectInputSteam = getClass().getResourceAsStream(SAMPLE_PROJECT_RELATIVE_PATH);

    @Before
    public void setup() throws IOException {
        resetSampleProjectFileToWritable();
    }

    @AfterClass
    public static void teardown() throws IOException {
        FileUtils.deleteDirectory(TEMPORARY_FOLDER);
    }

    @Test
    public void projectLoadedFromInputStreamCanBeSaved() throws IOException {
        Project project = new WsdlProject(sampleProjectInputSteam, null);
        answerYesWhenTheOverwriteDialogIsShown();
        SaveStatus status = project.save();
        assertThat(status, is(SaveStatus.SUCCESS));
    }

    @Test
    public void projectLoadedFromFileCanBeSaved() throws IOException {
        Project project = new WsdlProject(SAMPLE_PROJECT_ABSOLUTE_PATH, (WorkspaceImpl) null);
        SaveStatus status = project.save();
        assertThat(status, is(SaveStatus.SUCCESS));
    }

    @Test
    public void newlyCreatedProjectCanBeSaved() throws XmlException, IOException, SoapUIException {
        WsdlProject project = createTemporaryProject();
        SaveStatus status = project.saveIn(createTemporaryProjectFile());
        assertThat(status, is(SaveStatus.SUCCESS));
    }

    @Test
    public void userIsPromptedForSaveLocationWhenSavingProjectLoadedFromInputStream() throws IOException {
        Project project = new WsdlProject(sampleProjectInputSteam, null);
        answerYesWhenTheOverwriteDialogIsShown();
        project.save();
        verifyThatTheSaveAsDialogIsShown();
    }

    @Test
    public void newlyCreatedProjectIsNotSavedIfUserOptsNotToSave() throws XmlException, IOException, SoapUIException {
        Project project = createTemporaryProject();
        cancelWhenTheSaveAsFileDialogIsShown();
        SaveStatus status = project.save();
        assertThat(status, is(not(SaveStatus.SUCCESS)));
    }

    @Test
    public void projectIsNotSavedIfSaveAsDialogIsCancelled() throws IOException {
        Project project = new WsdlProject(sampleProjectInputSteam, null);
        answerYesWhenTheOverwriteDialogIsShown();
        cancelWhenTheSaveAsFileDialogIsShown();
        SaveStatus status = project.save();
        assertThat(status, is(SaveStatus.CANCELLED));
    }

    @Test
    public void existingFileIsNotSavedIfNotWritableAndWeDontWantToSave() throws IOException {
        setFileWritePermission(SAMPLE_PROJECT_ABSOLUTE_PATH, false);
        Project project = new WsdlProject(SAMPLE_PROJECT_ABSOLUTE_PATH, (WorkspaceImpl) null);
        answerNoWhenTheDoYouWantToWriteToNewFileDialogIsShown();
        SaveStatus status = project.save();
        assertThat(status, is(SaveStatus.DONT_SAVE));
    }

    @Test
    public void existingFileIsCancelledIfNotWritableAndNoNewFileSelected() throws IOException {
        setFileWritePermission(SAMPLE_PROJECT_ABSOLUTE_PATH, false);
        Project project = new WsdlProject(SAMPLE_PROJECT_ABSOLUTE_PATH, (WorkspaceImpl) null);
        answerYesWhenTheDoYouWantToWriteToNewFileDialogIsShown();
        cancelWhenTheSaveAsFileDialogIsShown();
        SaveStatus status = project.save();
        assertThat(status, is(SaveStatus.CANCELLED));
    }


    private void answerYesWhenTheOverwriteDialogIsShown() {
        stubbedDialogs.mockConfirmWithReturnValue(true);
    }

    private void answerYesWhenTheDoYouWantToWriteToNewFileDialogIsShown() {

        stubbedDialogs.mockConfirmWithReturnValue(true);
    }

    private void answerNoWhenTheDoYouWantToWriteToNewFileDialogIsShown() {
        stubbedDialogs.mockConfirmWithReturnValue(false);
    }

    private void cancelWhenTheSaveAsFileDialogIsShown() {
        when(mockedFileDialogs.saveAs(anyObject(), anyString(), anyString(), anyString(), isA(File.class))).thenReturn(null);
    }

    private void verifyThatTheSaveAsDialogIsShown() {
        verify(mockedFileDialogs).saveAs(anyObject(), anyString(), anyString(), anyString(), isA(File.class));
    }

    private File createTemporaryProjectFile() {
        return new File(TEMPORARY_FOLDER + File.separator + UUID.randomUUID() + "-soapui-project.xml");
    }

    private WsdlProject createTemporaryProject() throws XmlException, IOException, SoapUIException {
        WsdlProject project = new WsdlProject();
        project.setName(PROJECT_NAME);
        return project;
    }

    private void setFileWritePermission(String projectFilePath, boolean writable) throws IOException {
        boolean couldSetWritable = new File(projectFilePath).setWritable(writable);
        if (!couldSetWritable) {
            throw new IOException("Can't set project file '" + projectFilePath + "' to writable");
        }
    }

    private void resetSampleProjectFileToWritable() throws IOException {
        setFileWritePermission(SAMPLE_PROJECT_ABSOLUTE_PATH, true);
    }
}
