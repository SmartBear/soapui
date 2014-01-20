package com.eviware.soapui.plugins;

/**
 * Represents a plugin version
 */
public class Version
{
	private final int majorVersion;
	private final int minorVersion;
	private final String patchVersion;

	public static Version fromString( String versionString )
	{
		try
		{
			String[] parts = versionString.split( "\\." );
			String patchVersion = parts.length == 3 ? parts[2] : null;
			return new Version(Integer.parseInt( parts[0]),  Integer.parseInt( parts[1]), patchVersion);
		}
		catch( NumberFormatException e )
		{
			throw new IllegalArgumentException( versionString + " is not a valid version string" );
		}
	}

	public Version( int majorVersion, int minorVersion, String patchVersion )
	{
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.patchVersion = patchVersion;
	}

	@Override
	public String toString()
	{
		return majorVersion + '.' + minorVersion + (patchVersion == null ? "" : '.' + patchVersion);
	}
}
