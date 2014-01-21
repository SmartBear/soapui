/*
 * SoapUI, copyright (C) 2004-2013 smartbear.com
 *
 * SoapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.wsdl.panels.teststeps.support;

import com.eviware.soapui.impl.rest.panels.resource.RestParamsTableModel;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AddParamAction;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.JTableFixture;
import org.fest.swing.security.ExitCallHook;
import org.fest.swing.security.NoExitSecurityManagerInstaller;
import org.junit.*;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.fest.swing.data.TableCell.row;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Ignore
/**
 * @author Prakash
 */
public class AddParamActionIT
{
	public static final String PARAM = "Param";

	private static NoExitSecurityManagerInstaller noExitSecurityManagerInstaller;

	private JTable paramTable;
	private RestParamsPropertyHolder params;
	private static Robot robot;

	@BeforeClass
	public static void setUpOnce()
	{
		noExitSecurityManagerInstaller = NoExitSecurityManagerInstaller.installNoExitSecurityManager( new ExitCallHook()
		{
			@Override
			public void exitCalled( int status )
			{
				System.out.print( "Exit status : " + status );
			}
		} );
	}

	@AfterClass
	public static void classTearDown()
	{
		noExitSecurityManagerInstaller.uninstall();
	}


	@Before
	public void setUp() throws SoapUIException
	{
		robot = BasicRobot.robotWithCurrentAwtHierarchy();
		params = ModelItemFactory.makeRestRequest().getParams();
		paramTable = new JTable( new RestParamsTableModel( params ) );
	}

	@After
	public void tearDown()
	{
		robot.cleanUp();
	}

	@Test
	public void editsTheValueCellAfterNameCell() throws Exception
	{
		setCellEditorForNameAndValueColumns( PARAM );

		invokeAddParamAction();

		JTableFixture jTableFixture = new JTableFixture( robot, paramTable );

		verifyEditingCell( 0, 0 );

		jTableFixture.cell( row( 0 ).column( 0 ) ).stopEditing();

		verifyEditingCell( 0, 1 );

		jTableFixture.cell( row( 0 ).column( 1 ) ).stopEditing();

	}

	@Test
	public void removesThePropertyIfPropertyNameIsEmpty() throws Exception
	{
		setCellEditorForNameAndValueColumns( "" );

		invokeAddParamAction();

		JTableFixture jTableFixture = new JTableFixture( robot, paramTable );

		verifyEditingCell( 0, 0 );

		jTableFixture.cell( row( 0 ).column( 0 ) ).stopEditing();

		assertThat( params.getPropertyCount(), is( 0 ) );
	}

	@Test
	public void removesThePropertyIfEditingIsCancelledOnNameCell() throws Exception
	{
		setCellEditorForNameAndValueColumns( "" );

		invokeAddParamAction();

		JTableFixture jTableFixture = new JTableFixture( robot, paramTable );

		verifyEditingCell( 0, 0 );

		jTableFixture.cell( row( 0 ).column( 0 ) ).cancelEditing();

		assertThat( params.getPropertyCount(), is( 0 ) );
	}

	private void setCellEditorForNameAndValueColumns( final String value )
	{
		paramTable.setDefaultEditor( String.class, new DefaultCellEditor( new JTextField() )
		{
			@Override
			public Object getCellEditorValue()
			{
				return value;
			}
		} );
	}

	private void invokeAddParamAction()
	{
		ActionEvent actionEvent = Mockito.mock( ActionEvent.class );
		new AddParamAction( paramTable, params, "Add Param" ).actionPerformed( actionEvent );
	}

	private void verifyEditingCell( int row, int column ) throws InterruptedException
	{
		robot.waitForIdle();
		assertThat( paramTable.getEditingRow(), is( row ) );
		assertThat( paramTable.getEditingColumn(), is( column ) );
	}
}
