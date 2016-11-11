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
import com.eviware.x.dialogs.XDialogs;
import com.eviware.x.dialogs.XFileDialogs;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

/**
 * This class provides a base for mocking out dialogs in SoapUI when doing unit testing.
 * The dialogs will be replaced by <i>StubbedDialogs</i> and the file dialogs with a mock that
 * returns a new <i>File</i> object when the <i>saveAs()</i> method is called
 *
 * @see StubbedDialogs
 */
public abstract class StubbedDialogsTestBase {
    private static final String SAVED_PROJECT_FILE_NAME = "saved-project-file";
    private static final String SAVED_PROJECT_FILE_EXTENSION = ".xml";

    protected StubbedDialogs stubbedDialogs;
    @Mock
    protected XFileDialogs mockedFileDialogs;

    private XDialogs originalDialogs;
    private XFileDialogs originalFileDialogs;

    @Before
    public void setupStubbedDialogs() throws IOException {
        MockitoAnnotations.initMocks(this);
        // These need to be reset each time to support the
        stubbedDialogs = new StubbedDialogs();
        addSaveAsBehaviour(mockedFileDialogs);
        setMockedDialogsTemporary();
    }

    @After
    public void teardownStubbedDialogs() {
        restoreOriginalDialogs();
    }


    private void addSaveAsBehaviour(XFileDialogs mockedFileDialogs) throws IOException {
        File savedFile = File.createTempFile(SAVED_PROJECT_FILE_NAME, SAVED_PROJECT_FILE_EXTENSION);
        when(mockedFileDialogs.saveAs(anyObject(), anyString(), anyString(), anyString(), isA(File.class))).thenReturn(savedFile);
    }

    private void setMockedDialogsTemporary() {
        originalDialogs = UISupport.getDialogs();
        originalFileDialogs = UISupport.getFileDialogs();
        UISupport.setDialogs(stubbedDialogs);
        UISupport.setFileDialogs(mockedFileDialogs);
    }

    private void restoreOriginalDialogs() {
        UISupport.setDialogs(originalDialogs);
        UISupport.setFileDialogs(originalFileDialogs);
    }
}
