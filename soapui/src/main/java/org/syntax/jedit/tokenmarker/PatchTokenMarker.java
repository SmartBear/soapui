/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package org.syntax.jedit.tokenmarker;

import javax.swing.text.Segment;

/**
 * Patch/diff token marker.
 * 
 * @author Slava Pestov
 * @version $Id: PatchTokenMarker.java,v 1.7 1999/12/13 03:40:30 sp Exp $
 */
public class PatchTokenMarker extends TokenMarker
{
	public byte markTokensImpl( byte token, Segment line, int lineIndex )
	{
		if( line.count == 0 )
			return Token.NULL;
		switch( line.array[line.offset] )
		{
		case '+' :
		case '>' :
			addToken( line.count, Token.KEYWORD1 );
			break;
		case '-' :
		case '<' :
			addToken( line.count, Token.KEYWORD2 );
			break;
		case '@' :
		case '*' :
			addToken( line.count, Token.KEYWORD3 );
			break;
		default :
			addToken( line.count, Token.NULL );
			break;
		}
		return Token.NULL;
	}

	public boolean supportsMultilineTokens()
	{
		return false;
	}
}
