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
public class StubbedDialogsTest extends StubbedDialogsTestBase
{

	@Test
	public void catchesErrorMessage() throws Exception
	{
		String errorMessage = "The shit's hit the fan!";

		UISupport.showErrorMessage( errorMessage );
		assertThat( stubbedDialogs.getErrorMessages(), hasItem( errorMessage ) );
	}

	@Test
	public void catchesInfoMessages() throws Exception
	{
		String infoMessage = "Some info";

		UISupport.showInfoMessage( infoMessage );
		assertThat( stubbedDialogs.getInfoMessages(), hasItem( infoMessage ) );
	}

	@Test
	public void catchesConfirmQuestion()
	{
		String question = "Are you sure?";

		UISupport.confirm( question, "title" );
		assertThat( stubbedDialogs.getConfirmations(), hasConfirmationWithQuestion( question ) );
	}

	@Test
	public void canMockPositiveConfirmResult()
	{
		stubbedDialogs.mockConfirmWithReturnValue( true );

		boolean reply = UISupport.confirm( "", "" );
		assertThat( reply, equalTo( true ) );
	}

	@Test
	public void canMockNegativeConfirmResult()
	{
		stubbedDialogs.mockConfirmWithReturnValue( false );

		boolean reply = UISupport.confirm( "", "" );
		assertThat( reply, equalTo( false ) );
	}

	@Test
	public void canMockNullConfirmResult()
	{
		stubbedDialogs.mockConfirmWithReturnValue( null );

		Boolean reply = UISupport.confirmOrCancel( "", "" );
		assertThat( reply, nullValue() );
	}

	@Test
	public void canMockMultipleReturnValuesForConfirmation()
	{
		stubbedDialogs.mockConfirmWithReturnValue( true, false, null );

		assertThat( UISupport.confirmOrCancel( "", "" ), equalTo( true ) );
		assertThat( UISupport.confirmOrCancel( "", "" ), equalTo( false ) );
		assertThat( UISupport.confirmOrCancel( "", "" ), nullValue() );
	}

	@Test
	public void returnsLastMockedValueIfMoreInvocationsThanValues()
	{
		stubbedDialogs.mockConfirmWithReturnValue( true, false );

		assertThat( UISupport.confirmOrCancel( "", "" ), equalTo( true ) );
		assertThat( UISupport.confirmOrCancel( "", "" ), equalTo( false ) );
		assertThat( UISupport.confirmOrCancel( "", "" ), equalTo( false ) );
		assertThat( UISupport.confirmOrCancel( "", "" ), equalTo( false ) );
	}
}

