package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.config.TestSuiteConfig;

import static com.eviware.soapui.utils.ModelItemMatchers.belongsTo;
import static com.eviware.soapui.utils.ModelItemMatchers.hasATestCaseNamed;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Created with IntelliJ IDEA.
 * User: manne
 * Date: 8/28/13
 * Time: 3:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class WsdlTestSuiteTest
{

	private WsdlTestSuite suite;
	private WsdlProject project;

	@Before
	public void setUp() throws Exception
	{
		project = mock(WsdlProject.class );
		suite = new WsdlTestSuite( project, TestSuiteConfig.Factory.newInstance() );
	}

	@Test
	public void referencesProject() {
		assertThat(suite, belongsTo( project ));
	}

	@Test
	public void addsTestCasesForNames() throws Exception
	{
		String testCaseName = "Frakking big test case";
		suite.addNewTestCase( testCaseName );
		assertThat( suite, hasATestCaseNamed( testCaseName ) );

	}

	@Test
	public void doesNotAddTestCaseWithNullName() throws Exception
	{
		try
		{
			suite.addNewTestCase( null );
		}
		catch( Exception e )
		{

		}
		assertThat( suite, not( hasATestCaseNamed( null ) ));

	}

}
