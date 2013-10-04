package com.eviware.soapui.cucumber.gui;

import com.eviware.soapui.SoapUI;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.Robot;
import org.fest.swing.exception.WaitTimedOutError;
import org.fest.swing.finder.DialogFinder;
import org.fest.swing.fixture.FrameFixture;

import java.awt.*;

import static org.fest.swing.finder.WindowFinder.findDialog;
import static org.fest.swing.finder.WindowFinder.findFrame;
import static org.fest.swing.launcher.ApplicationLauncher.application;

class FESTRunner
{

	Robot robot;
	FrameFixture frame;

	void startSoapUI() {

		robot = BasicRobot.robotWithCurrentAwtHierarchy();

		try {
			frame = findFrame(new GenericTypeMatcher<Frame>(Frame.class) {
				protected boolean isMatching(Frame frame) {
					return frame.getTitle().startsWith("SoapUI") && frame.isShowing();
				}
			}).using(robot);
		}
		catch (WaitTimedOutError e) {
			robot.cleanUp();
			application(SoapUI.class).start();
			robot = BasicRobot.robotWithCurrentAwtHierarchy();
			frame = findFrame(new GenericTypeMatcher<Frame>(Frame.class) {
				protected boolean isMatching(Frame frame) {
					return frame.getTitle().startsWith("SoapUI") && frame.isShowing();
				}
			}).using(robot);
		}
	}

	FrameFixture getFrame() {
		return frame;
	}

	Robot getRobot() {
		return robot;
	}

	void closeOpenDialogs() {
		try {
			DialogFinder dialog = findDialog( new GenericTypeMatcher<Dialog>( Dialog.class )
			{
				@Override
				protected boolean isMatching( Dialog t )
				{
					return t.isVisible();
				}
			} ).withTimeout( 1000 );
			dialog.using(getRobot()).close();

		}
		catch (WaitTimedOutError e) {}
	}

	void shutdown() {
		closeOpenDialogs();
		getRobot().cleanUpWithoutDisposingWindows();
	}
}
