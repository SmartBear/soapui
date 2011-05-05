package com.eviware.soapui.security.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;

public class RandomFile
{

	private File file;
	private long length;

	private final Random random = new Random();

	public RandomFile( long length, String name, String contentType ) throws IOException
	{
		this.length = length;
		file = File.createTempFile( StringUtils.createFileName( name, '-' ),
				"." + ContentTypeHandler.getExtensionForContentType( contentType ) );
	}

	public File next() throws IOException
	{
		BufferedWriter out = new BufferedWriter( new FileWriter( file ) );
		long used = 0;

		while( used <= length )
		{
			used++ ;
			out.write( random.nextInt() );
		}
		out.flush();
		out.close();

		return file;
	}

}
