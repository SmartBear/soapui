package com.eviware.soapui.impl.wadl;

import net.java.dev.wadl.x2009.x02.ApplicationDocument;
import net.java.dev.wadl.x2009.x02.ApplicationDocument.Application;

import junit.framework.TestCase;

public class WadlTestCase extends TestCase
{
	public void testWadl() throws Exception
	{
		ApplicationDocument applicationDocument = ApplicationDocument.Factory.newInstance();
		Application application = applicationDocument.addNewApplication();
	}
}
