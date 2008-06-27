package com.eviware.soapui.impl.wadl;

import com.sun.research.wadl.x2006.x10.ApplicationDocument;
import com.sun.research.wadl.x2006.x10.ApplicationDocument.Application;

import junit.framework.TestCase;

public class WadlTestCase extends TestCase
{
	public void testWadl() throws Exception
	{
		ApplicationDocument applicationDocument = ApplicationDocument.Factory.newInstance();
		Application application = applicationDocument.addNewApplication();
	}
}
