package com.eviware.soapui.utils;

import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.XDialogs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.eviware.soapui.utils.StubbedDialogs.hasConfirmationWithQuestion;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * Demo code for the StubbedDialogsTest functionality
 */
public class StubbedDialogsTest
{

	private StubbedDialogs dialogs;
	private XDialogs originalDialogs;

	@Before
	public void resetDialogs()
	{
		originalDialogs = UISupport.getDialogs();
		dialogs = new StubbedDialogs();
		UISupport.setDialogs( dialogs );
	}

	@Test
	public void catchesErrorMessage() throws Exception
	{
		String errorMessage = "The shit's hit the fan!";

		UISupport.showErrorMessage( errorMessage );
		assertThat( dialogs.getErrorMessages(), hasItem( errorMessage ) );
	}

	@Test
	public void catchesInfoMessages() throws Exception
	{
		String infoMessage = "Some info";

		UISupport.showInfoMessage( infoMessage );
		assertThat( dialogs.getInfoMessages(), hasItem( infoMessage ) );
	}

	@Test
	public void catchesConfirmQuestion()
	{
		String question = "Are you sure?";

		UISupport.confirm( question, "title" );
		assertThat( dialogs.getConfirmations(), hasConfirmationWithQuestion( question ) );
	}

	@Test
	public void canMockPositiveConfirmResult()
	{
		dialogs.mockConfirmWithReturnValue( true );

		boolean reply = UISupport.confirm( "", "" );
		assertThat( reply, equalTo( true ) );
	}

	@Test
	public void canMockNegativeConfirmResult()
	{
		dialogs.mockConfirmWithReturnValue( false );

		boolean reply = UISupport.confirm( "", "" );
		assertThat( reply, equalTo( false ) );
	}

	@Test
	public void canMockNullConfirmResult()
	{
		dialogs.mockConfirmWithReturnValue( null );

		Boolean reply = UISupport.confirmOrCancel( "", "" );
		assertThat( reply, nullValue() );
	}

	@Test
	public void canMockMultipleReturnValuesForConfirmation()
	{
		dialogs.mockConfirmWithReturnValue( true, false, null );

		assertThat( UISupport.confirmOrCancel( "", "" ), equalTo( true ) );
		assertThat( UISupport.confirmOrCancel( "", "" ), equalTo( false ) );
		assertThat( UISupport.confirmOrCancel( "", "" ), nullValue() );
	}

	@Test
	public void returnsLastMockedValueIfMoreInvocationsThanValues()
	{
		dialogs.mockConfirmWithReturnValue( true, false );

		assertThat( UISupport.confirmOrCancel( "", "" ), equalTo( true ) );
		assertThat( UISupport.confirmOrCancel( "", "" ), equalTo( false ) );
		assertThat( UISupport.confirmOrCancel( "", "" ), equalTo( false ) );
		assertThat( UISupport.confirmOrCancel( "", "" ), equalTo( false ) );
	}

	@After
	public void restoreDialogs()
	{
		UISupport.setDialogs( originalDialogs );
	}
}

