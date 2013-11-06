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
 * TeX token marker.
 * 
 * @author Slava Pestov
 * @version $Id: TeXTokenMarker.java,v 1.16 1999/12/13 03:40:30 sp Exp $
 */
public class TeXTokenMarker extends TokenMarker
{
	// public members
	public static final byte BDFORMULA = Token.INTERNAL_FIRST;
	public static final byte EDFORMULA = ( byte )( Token.INTERNAL_FIRST + 1 );

	public byte markTokensImpl( byte token, Segment line, int lineIndex )
	{
		char[] array = line.array;
		int offset = line.offset;
		int lastOffset = offset;
		int length = line.count + offset;
		boolean backslash = false;
		loop : for( int i = offset; i < length; i++ )
		{
			int i1 = ( i + 1 );

			char c = array[i];
			// if a backslash is followed immediately
			// by a non-alpha character, the command at
			// the non-alpha char. If we have a backslash,
			// some text, and then a non-alpha char,
			// the command ends before the non-alpha char.
			if( Character.isLetter( c ) )
			{
				backslash = false;
			}
			else
			{
				if( backslash )
				{
					// \<non alpha>
					// we skip over this character,
					// hence the `continue'
					backslash = false;
					if( token == Token.KEYWORD2 || token == EDFORMULA )
						token = Token.KEYWORD2;
					addToken( i1 - lastOffset, token );
					lastOffset = i1;
					if( token == Token.KEYWORD1 )
						token = Token.NULL;
					continue;
				}
				else
				{
					// \blah<non alpha>
					// we leave the character in
					// the stream, and it's not
					// part of the command token
					if( token == BDFORMULA || token == EDFORMULA )
						token = Token.KEYWORD2;
					addToken( i - lastOffset, token );
					if( token == Token.KEYWORD1 )
						token = Token.NULL;
					lastOffset = i;
				}
			}
			switch( c )
			{
			case '%' :
				if( backslash )
				{
					backslash = false;
					break;
				}
				addToken( i - lastOffset, token );
				addToken( length - i, Token.COMMENT1 );
				lastOffset = length;
				break loop;
			case '\\' :
				backslash = true;
				if( token == Token.NULL )
				{
					token = Token.KEYWORD1;
					addToken( i - lastOffset, Token.NULL );
					lastOffset = i;
				}
				break;
			case '$' :
				backslash = false;
				if( token == Token.NULL ) // singe $
				{
					token = Token.KEYWORD2;
					addToken( i - lastOffset, Token.NULL );
					lastOffset = i;
				}
				else if( token == Token.KEYWORD1 ) // \...$
				{
					token = Token.KEYWORD2;
					addToken( i - lastOffset, Token.KEYWORD1 );
					lastOffset = i;
				}
				else if( token == Token.KEYWORD2 ) // $$aaa
				{
					if( i - lastOffset == 1 && array[i - 1] == '$' )
					{
						token = BDFORMULA;
						break;
					}
					token = Token.NULL;
					addToken( i1 - lastOffset, Token.KEYWORD2 );
					lastOffset = i1;
				}
				else if( token == BDFORMULA ) // $$aaa$
				{
					token = EDFORMULA;
				}
				else if( token == EDFORMULA ) // $$aaa$$
				{
					token = Token.NULL;
					addToken( i1 - lastOffset, Token.KEYWORD2 );
					lastOffset = i1;
				}
				break;
			}
		}
		if( lastOffset != length )
			addToken( length - lastOffset, token == BDFORMULA || token == EDFORMULA ? Token.KEYWORD2 : token );
		return( token != Token.KEYWORD1 ? token : Token.NULL );
	}
}
