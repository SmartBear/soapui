package soapui.demo;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;

public class DemoListener extends TestRunListenerAdapter
{
	private long startTime;

	public void beforeRun( TestRunner testRunner, TestRunContext runContext )
	{
		startTime = System.nanoTime();
	}
	
	public void afterRun( TestRunner testRunner, TestRunContext runContext )
	{
		long endTime = System.nanoTime();
		SoapUI.log.info( "TestCase [" + testRunner.getTestCase().getName() + "] took " + (endTime-startTime) + " nanoseconds." );
	}
}
