package com.eviware.soapui.monitor;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 *
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 */
public class NaughtyTest
    extends AbstractTestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public NaughtyTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( NaughtyTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        // Crash and burn!
    }
}
