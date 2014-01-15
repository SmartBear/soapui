package com.eviware.soapui.impl.wsdl.endpoint;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.JTableFixture;
import org.fest.swing.security.ExitCallHook;
import org.fest.swing.security.NoExitSecurityManagerInstaller;
import org.junit.*;

@Ignore
/**
 * FEST-based test verifying parts of the functionality of DefaultEndpointStrategyConfigurationPanel.
 */
public class DefaultEndpointStrategyConfigurationPanelIT
{
	private Robot robot;
	private RestService restService;
	private DefaultEndpointStrategyConfigurationPanel configurationPanel;

	private static NoExitSecurityManagerInstaller noExitSecurityManagerInstaller;

	@BeforeClass
	public static void setUpOnce()
	{
		noExitSecurityManagerInstaller = NoExitSecurityManagerInstaller.installNoExitSecurityManager( new ExitCallHook()
		{
			@Override
			public void exitCalled( int status )
			{
				System.out.print( "Exit status : " + status );
			}
		} );
	}

	@AfterClass
	public static void classTearDown()
	{
		noExitSecurityManagerInstaller.uninstall();
	}

	@Before
	public void setUp() throws SoapUIException
	{
		robot = BasicRobot.robotWithCurrentAwtHierarchy();
		restService = ModelItemFactory.makeRestService();
		DefaultEndpointStrategy strategy = new DefaultEndpointStrategy();
		strategy.init( restService.getProject() );
		configurationPanel = new DefaultEndpointStrategyConfigurationPanel( restService, strategy );
	}

	@Test
	public void updatesTableWithNewEndpoint() throws Exception
	{
		JTableFixture tableFixture = new JTableFixture( robot, configurationPanel.table );

		String endpoint = "http://sljll.com";
		restService.addEndpoint( endpoint );
		// this call will fail if the new endpoint is not found in the table
		tableFixture.cell( endpoint );
	}

	@After
	public void tearDown()
	{
		robot.cleanUp();
	}
}
