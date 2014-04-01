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
*/package com.eviware.soapui.utils;

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
