package com.eviware.soapui.utils;

import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.XDialogs;
import org.junit.After;
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
		assertThat(dialogs.getErrorMessages(), hasItem(errorMessage));
	}

	@Test
	public void catchesInfoMessages() throws Exception
	{
		String infoMessage = "Some info";

		UISupport.showInfoMessage( infoMessage );
		assertThat(dialogs.getInfoMessages(), hasItem(infoMessage));
	}

	@After
	public void restoreDialogs() {
		 UISupport.setDialogs( originalDialogs );
	}
}
