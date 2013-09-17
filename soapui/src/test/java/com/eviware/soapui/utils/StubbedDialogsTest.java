package com.eviware.soapui.utils;

import com.eviware.soapui.support.UISupport;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 *  Demo code for the StubbedDialogsTest functionality
 */
public class StubbedDialogsTest
{

	private StubbedDialogs dialogs;

	@Before
	public void resetDialogs()
	{
		dialogs = new StubbedDialogs();
		UISupport.setDialogs( dialogs );
	}

	@Test
	public void catchesErrorMessage() throws Exception
	{
		String errorMessage = "The shit's hit the fan!";

		UISupport.showErrorMessage( errorMessage );
		assertThat(dialogs.getErrorMessages(), hasItem(errorMessage));
	}

	@Test
	public void catchesInfoMessages() throws Exception
	{
		String infoMessage = "Some info";

		UISupport.showInfoMessage( infoMessage );
		assertThat(dialogs.getInfoMessages(), hasItem(infoMessage));
	}
}
