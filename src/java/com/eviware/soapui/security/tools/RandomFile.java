package com.eviware.soapui.security.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class RandomFile
{

	private File file;
	private long length;

	private final Random random = new Random();

	public RandomFile( int length, String name ) throws IOException
	{
		this.length = length;
		file = File.createTempFile( "attachment-" + name, ".xxx" );
	}

	public void next() throws IOException
	{
		FileOutputStream out = new FileOutputStream( file );
		long used = 0;

		while( used <= length )
		{
			used++ ;
			out.write( random.nextInt() );
		}
		out.close();
	}

}
