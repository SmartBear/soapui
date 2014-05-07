/*
 * Copyright 2004-2014 SmartBear Software
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

package com.smartbear.soapui.other.rest.oauth2;

import com.eviware.soapui.impl.rest.panels.resource.RestParamsTableModel;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import com.smartbear.soapui.utils.IntegrationTest;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.JTableFixture;
import org.fest.swing.security.ExitCallHook;
import org.fest.swing.security.NoExitSecurityManagerInstaller;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.fest.swing.data.TableCell.row;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Prakash
 */
@Category(IntegrationTest.class)
public class AddParamActionTest {
    public static final String PARAM = "Param";
    private static final int WAIT_FOR_LAST_TEST_TO_SHUTDOWN = 3000;

    private static NoExitSecurityManagerInstaller noExitSecurityManagerInstaller;

    private JTable paramTable;
    private RestParamsPropertyHolder params;
    private static Robot robot;

    @BeforeClass
    public static void setUpOnce() {
        noExitSecurityManagerInstaller = NoExitSecurityManagerInstaller.installNoExitSecurityManager(new ExitCallHook() {
            @Override
            public void exitCalled(int status) {
                System.out.print("Exit status : " + status);
            }
        });
    }

    @AfterClass
    public static void classTearDown() throws InterruptedException {
        // TODO This is needed to ensure that the last test have stopped before uninstalling, we need a more
        // clever way to wait for the test though
        Thread.sleep(WAIT_FOR_LAST_TEST_TO_SHUTDOWN);
        noExitSecurityManagerInstaller.uninstall();
    }


    @Before
    public void setUp() throws SoapUIException {
        robot = BasicRobot.robotWithCurrentAwtHierarchy();
        params = ModelItemFactory.makeRestRequest().getParams();
        paramTable = new JTable(new RestParamsTableModel(params));
    }

    @After
    public void tearDown() {
        robot.cleanUp();
    }

    @Test
    public void editsTheValueCellAfterNameCell() throws Exception {
        setCellEditorForNameAndValueColumns(PARAM);

        invokeAddParamAction();

        JTableFixture jTableFixture = new JTableFixture(robot, paramTable);

        verifyEditingCell(0, 0);

        jTableFixture.cell(row(0).column(0)).stopEditing();

        verifyEditingCell(0, 1);

        jTableFixture.cell(row(0).column(1)).stopEditing();

    }

    @Test
    public void removesThePropertyIfPropertyNameIsEmpty() throws Exception {
        setCellEditorForNameAndValueColumns("");

        invokeAddParamAction();

        JTableFixture jTableFixture = new JTableFixture(robot, paramTable);

        verifyEditingCell(0, 0);

        jTableFixture.cell(row(0).column(0)).stopEditing();

        assertThat(params.getPropertyCount(), is(0));
    }

    @Test
    public void removesThePropertyIfEditingIsCancelledOnNameCell() throws Exception {
        setCellEditorForNameAndValueColumns("");

        invokeAddParamAction();

        JTableFixture jTableFixture = new JTableFixture(robot, paramTable);

        verifyEditingCell(0, 0);

        jTableFixture.cell(row(0).column(0)).cancelEditing();

        assertThat(params.getPropertyCount(), is(0));
    }

    private void setCellEditorForNameAndValueColumns(final String value) {
        paramTable.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()) {
            @Override
            public Object getCellEditorValue() {
                return value;
            }
        });
    }

    private void invokeAddParamAction() {
        ActionEvent actionEvent = Mockito.mock(ActionEvent.class);
        new com.eviware.soapui.impl.wsdl.panels.teststeps.support.AddParamAction(paramTable, params, "Add Param").actionPerformed(actionEvent);
    }

    private void verifyEditingCell(int row, int column) throws InterruptedException {
        robot.waitForIdle();
        assertThat(paramTable.getEditingRow(), is(row));
        assertThat(paramTable.getEditingColumn(), is(column));
    }
}
