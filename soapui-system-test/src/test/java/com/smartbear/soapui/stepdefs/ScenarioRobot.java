package com.smartbear.soapui.stepdefs;

import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.Robot;

import static com.smartbear.soapui.utils.fest.FestMatchers.frameWithTitle;

/**
	A wapper for an AWT robot that's instantiated once per scenario and passed to  stepDefs using constructor injection
 */
public final class ScenarioRobot
{
	private final Robot robot;

	public ScenarioRobot()
	{
		robot = BasicRobot.robotWithNewAwtHierarchy();
	}

	public Robot getRobot()
	{
		return robot;
	}
}