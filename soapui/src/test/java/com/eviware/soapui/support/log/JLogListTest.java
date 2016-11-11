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

package com.eviware.soapui.support.log;

import org.junit.Before;
import org.junit.Test;

import javax.swing.ListModel;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for JLogList.
 */
public class JLogListTest {

    private JLogList logList;
    private ListModel model;

    @Before
    public void setUp() throws Exception {
        logList = new JLogList("Test log list");
        model = logList.getLogList().getModel();
    }

    @Test
    public void limitsTheNumberOfRows() throws Exception {
        final int maxRows = 10;
        logList.setMaxRows(10);
        for (int i = 0; i < maxRows + 1; i++) {
            logList.addLine("Line " + i);
        }
        waitForUpdaterThread();

        assertThat(model.getSize(), is(maxRows));
        assertThat((String) model.getElementAt(0), is("Line 1"));
    }

    @Test
    public void addsLogLinesInCorrectOrder() throws Exception {
        for (int i = 0; i < 20 + 1; i++) {
            logList.addLine("Line " + i);
        }
        waitForUpdaterThread();

        for (int i = 0; i < 20 + 1; i++) {
            assertThat((String) model.getElementAt(i), is("Line " + i));
        }
    }

    @Test
    public void clearsLogListCorrectly() throws Exception {
        for (int i = 0; i < 20 + 1; i++) {
            logList.addLine("Line " + i);
        }
        waitForUpdaterThread();
        logList.clear();
        waitForUpdaterThread();

        assertThat(model.getSize(), is(0));
    }


    private void waitForUpdaterThread() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignore) {

        }
    }

}
