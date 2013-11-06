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
 * Java properties/DOS INI token marker.
 * 
 * @author Slava Pestov
 * @version $Id: PropsTokenMarker.java,v 1.9 1999/12/13 03:40:30 sp Exp $
 */
public class PropsTokenMarker extends TokenMarker
{
	public static final byte VALUE = Token.INTERNAL_FIRST;

	public byte markTokensImpl( byte token, Segment line, int lineIndex )
	{
		char[] array = line.array;
		int offset = line.offset;
		int lastOffset = offset;
		int length = line.count + offset;
		loop : for( int i = offset; i < length; i++ )
		{
			int i1 = ( i + 1 );

			switch( token )
			{
			case Token.NULL :
				switch( array[i] )
				{
				case '#' :
				case ';' :
					if( i == offset )
					{
						addToken( line.count, Token.COMMENT1 );
						lastOffset = length;
						break loop;
					}
					break;
				case '[' :
					if( i == offset )
					{
						addToken( i - lastOffset, token );
						token = Token.KEYWORD2;
						lastOffset = i;
					}
					break;
				case '=' :
					addToken( i - lastOffset, Token.KEYWORD1 );
					token = VALUE;
					lastOffset = i;
					break;
				}
				break;
			case Token.KEYWORD2 :
				if( array[i] == ']' )
				{
					addToken( i1 - lastOffset, token );
					token = Token.NULL;
					lastOffset = i1;
				}
				break;
			case VALUE :
				break;
			default :
				throw new InternalError( "Invalid state: " + token );
			}
		}
		if( lastOffset != length )
			addToken( length - lastOffset, Token.NULL );
		return Token.NULL;
	}

	public boolean supportsMultilineTokens()
	{
		return false;
	}
}
