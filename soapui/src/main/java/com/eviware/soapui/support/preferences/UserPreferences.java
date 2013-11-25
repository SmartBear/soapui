package com.eviware.soapui.support.preferences;

import com.eviware.soapui.SoapUI;

import java.awt.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * This class uses user specific preferences that do not belong in a project file (since it may be shared between
 * users), such as preferred window position and save locations.
 */
public class UserPreferences
{

	public static final String ROOT_NODE_NAME = SoapUI.class.getName();

	// package protected to help unit test
	static final String WINDOW_X = "SoapUIWindowX";
	static final String WINDOW_Y = "SoapUIWindowY";
	static final String WINDOW_WIDTH = "SoapUIWindowWidth";
	static final String WINDOW_HEIGHT = "SoapUIWindowHeight";
	static final String EXTENDED_STATE = "SoapUIExtendedState";

	private Preferences preferences = Preferences.userRoot().node( ROOT_NODE_NAME );

	public void setSoapUIWindowBounds( Rectangle windowBounds ) throws BackingStoreException
	{
		if (windowBounds == null)
		{
			clearAllProperties(WINDOW_X, WINDOW_Y, WINDOW_HEIGHT, WINDOW_WIDTH);
			return;
		}
		preferences.putInt(WINDOW_X, windowBounds.x);
		preferences.putInt(WINDOW_Y, windowBounds.y);
		preferences.putInt(WINDOW_WIDTH, windowBounds.width);
		preferences.putInt( WINDOW_HEIGHT, windowBounds.height );
		preferences.flush();
	}

	public Rectangle getSoapUIWindowBounds()
	{
			if ( hasAllIntProperties( WINDOW_X, WINDOW_Y, WINDOW_WIDTH, WINDOW_HEIGHT ) )
			{
				 return new Rectangle(preferences.getInt(WINDOW_X, 0), preferences.getInt( WINDOW_Y, 0 ),
						 preferences.getInt(WINDOW_WIDTH, 800), preferences.getInt( WINDOW_HEIGHT, 600 ));
			}
			else
			{
				return null;
			}
	}

	public void setSoapUIExtendedState( int extendedState ) throws BackingStoreException
	{
		preferences.putInt( EXTENDED_STATE, extendedState );
		preferences.flush();
	}

	public int getSoapUIExtendedState()
	{
			if ( hasAllIntProperties( EXTENDED_STATE ) )
			{
				 return preferences.getInt( EXTENDED_STATE, Frame.NORMAL );
			}
			else
			{
				return Frame.NORMAL;
			}
	}

	private void clearAllProperties( String... propertyNames)
	{
		for( String propertyName : propertyNames )
		{
			preferences.remove( propertyName );
		}
	}

	private boolean hasAllIntProperties( String... propertyNames )
	{
		for( String propertyName : propertyNames )
		{
			if( preferences.getInt( propertyName, -1 ) == -1)
			{
				return false;
			}
		}
		return true;
	}

	public static void main( String[] args ) throws BackingStoreException
	{
		new UserPreferences().setSoapUIWindowBounds( null );
	}
}
