package com.eviware.soapui.utils;

import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.XDialogs;
import org.junit.After;
import org.junit.Before;

/**
 * Created with IntelliJ IDEA.
 * User: manne
 * Date: 2/17/14
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class StubbedDialogsTestBase
{
	protected StubbedDialogs stubbedDialogs;
	private XDialogs originalDialogs;

	@Before
	public void resetDialogs()
	{
		originalDialogs = UISupport.getDialogs();
		stubbedDialogs = new StubbedDialogs();
		UISupport.setDialogs( stubbedDialogs );
	}

	@After
	public void restoreDialogs()
	{
		UISupport.setDialogs( originalDialogs );
	}
}
