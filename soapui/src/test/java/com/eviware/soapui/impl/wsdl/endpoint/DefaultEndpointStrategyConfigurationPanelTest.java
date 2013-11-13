package com.eviware.soapui.impl.wsdl.endpoint;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.JTableFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * FEST-based test verifying parts of the functionality of DefaultEndpointStrategyConfigurationPanel.
 */
public class DefaultEndpointStrategyConfigurationPanelTest
{
	private Robot robot;
	private RestService restService;
	private DefaultEndpointStrategyConfigurationPanel configurationPanel;

	@Before
	public void setUp() throws SoapUIException
	{
		robot = BasicRobot.robotWithCurrentAwtHierarchy();
		restService = ModelItemFactory.makeRestService();
		DefaultEndpointStrategy strategy = new DefaultEndpointStrategy();
		strategy.init(restService.getProject());
		configurationPanel = new DefaultEndpointStrategyConfigurationPanel( restService, strategy );
	}

	@Test
	public void updatesTableWithNewEndpoint() throws Exception
	{
		JTableFixture tableFixture = new JTableFixture( robot, configurationPanel.table );

		String endpoint= "http://sljll.com";
		restService.addEndpoint( endpoint );
		// this call will fail if the new endpoint is not found in the table
		tableFixture.cell(endpoint);
	}

	@After
	public void tearDown()
	{
		robot.cleanUp();
	}
}
