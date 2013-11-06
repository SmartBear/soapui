package com.eviware.soapui.tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SplashScreenStomper
{
	public static void main( String arg[] ) throws IOException
	{
		String version = arg[0];
		String inputFilePath = arg[1];
		String outputFilePath = arg[2];

		String fullText = String.format( "%s\n\nCopyright SmartBear Software\n\nwww.soapui.org\nwww.smartbear.com", version );
		BufferedImage bufferedImage = ImageIO.read( new File( inputFilePath ) );
		Graphics2D graphics = bufferedImage.createGraphics();
		graphics.setColor( new Color( 0x66, 0x66, 0x66 ) );
		Font font = new Font( "Arial", Font.PLAIN, 12 );
		graphics.setFont( font );
		graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
		int y = 181;
		for( String text : fullText.split( "\n" ) )
		{
			graphics.drawString( text, 42, y );
			y += 15;
		}

		ImageIO.write( bufferedImage, "png", new File( outputFilePath ) );

	}
}