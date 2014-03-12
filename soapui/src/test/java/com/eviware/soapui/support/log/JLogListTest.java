package com.eviware.soapui.support.log;

import org.junit.Before;
import org.junit.Test;

import javax.swing.ListModel;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for JLogList.
 */
public class JLogListTest
{

	private JLogList logList;
	private ListModel model;

	@Before
	public void setUp() throws Exception
	{
		logList = new JLogList( "Test log list" );
		model = logList.getLogList().getModel();
	}

	@Test
	public void limitsTheNumberOfRows() throws Exception
	{
		final int maxRows = 10;
		logList.setMaxRows( 10 );
		for( int i = 0; i < maxRows + 1; i++ )
		{
			logList.addLine( "Line " + i );
		}
			waitForUpdaterThread();

		assertThat( model.getSize(), is( maxRows ) );
		assertThat( ( String )model.getElementAt( 0 ), is( "Line 1" ) );
	}

	@Test
	public void addsLogLinesInCorrectOrder() throws Exception
	{
		for( int i = 0; i < 20 + 1; i++ )
		{
			logList.addLine( "Line " + i );
		}
		waitForUpdaterThread();

		for( int i = 0; i < 20 + 1; i++ )
		{
			assertThat( (String)model.getElementAt( i ), is( "Line " + i ) );
		}
	}

	@Test
	public void clearsLogListCorrectly() throws Exception
	{
		for( int i = 0; i < 20 + 1; i++ )
		{
			logList.addLine( "Line " + i );
		}
		waitForUpdaterThread();
		logList.clear();
		waitForUpdaterThread();

		assertThat(model.getSize(), is(0));
	}


	private void waitForUpdaterThread()
	{
		try
		{
			Thread.sleep( 20 );
		}
		catch( InterruptedException ignore )
		{

		}
	}

}
