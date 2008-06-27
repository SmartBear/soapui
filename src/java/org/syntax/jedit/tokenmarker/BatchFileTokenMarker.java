/*
 * BatchFileTokenMarker.java - Batch file token marker
 * Copyright (C) 1998, 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package org.syntax.jedit.tokenmarker;

import javax.swing.text.Segment;

import org.syntax.jedit.SyntaxUtilities;

/**
 * Batch file token marker.
 *
 * @author Slava Pestov
 * @version $Id: BatchFileTokenMarker.java,v 1.20 1999/12/13 03:40:29 sp Exp $
 */
public class BatchFileTokenMarker extends TokenMarker
{
	public byte markTokensImpl(byte token, Segment line, int lineIndex)
	{
		char[] array = line.array;
		int offset = line.offset;
		int lastOffset = offset;
		int length = line.count + offset;

		if(SyntaxUtilities.regionMatches(true,line,offset,"rem"))
		{
			addToken(line.count,Token.COMMENT1);
			return Token.NULL;
		}

loop:		for(int i = offset; i < length; i++)
		{
			int i1 = (i+1);

			switch(token)
			{
			case Token.NULL:
				switch(array[i])
				{
				case '%':
					addToken(i - lastOffset,token);
					lastOffset = i;
					if(length - i <= 3 || array[i+2] == ' ')
					{
						addToken(2,Token.KEYWORD2);
						i += 2;
						lastOffset = i;
					}
					else
						token = Token.KEYWORD2;
					break;
				case '"':
					addToken(i - lastOffset,token);
					token = Token.LITERAL1;
					lastOffset = i;
					break;
				case ':':
					if(i == offset)
					{
						addToken(line.count,Token.LABEL);
						lastOffset = length;
						break loop;
					}
					break;
				case ' ':
					if(lastOffset == offset)
					{
						addToken(i - lastOffset,Token.KEYWORD1);
						lastOffset = i;
					}
					break;
				}
				break;
			case Token.KEYWORD2:
				if(array[i] == '%')
				{
					addToken(i1 - lastOffset,token);
					token = Token.NULL;
					lastOffset = i1;
				}
				break;
			case Token.LITERAL1:
				if(array[i] == '"')
				{
					addToken(i1 - lastOffset,token);
					token = Token.NULL;
					lastOffset = i1;
				}
				break;
			default:
				throw new InternalError("Invalid state: " + token);
			}
		}

		if(lastOffset != length)
		{
			if(token != Token.NULL)
				token = Token.INVALID;
			else if(lastOffset == offset)
				token = Token.KEYWORD1;
			addToken(length - lastOffset,token);
		}
		return Token.NULL;
	}

	public boolean supportsMultilineTokens()
	{
		return false;
	}
}
