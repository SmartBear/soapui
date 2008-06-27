/*
 * MakefileTokenMarker.java - Makefile token marker
 * Copyright (C) 1998, 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package org.syntax.jedit.tokenmarker;

import javax.swing.text.Segment;

/**
 * Makefile token marker.
 *
 * @author Slava Pestov
 * @version $Id: MakefileTokenMarker.java,v 1.18 1999/12/13 03:40:30 sp Exp $
 */
public class MakefileTokenMarker extends TokenMarker
{
	// public members
	public byte markTokensImpl(byte token, Segment line, int lineIndex)
	{
		char[] array = line.array;
		int offset = line.offset;
		int lastOffset = offset;
		int length = line.count + offset;
		boolean backslash = false;
loop:		for(int i = offset; i < length; i++)
		{
			int i1 = (i+1);

			char c = array[i];
			if(c == '\\')
			{
				backslash = !backslash;
				continue;
			}

			switch(token)
			{
			case Token.NULL:
				switch(c)
				{
				case ':': case '=': case ' ': case '\t':
					backslash = false;
					if(lastOffset == offset)
					{
						addToken(i1 - lastOffset,Token.KEYWORD1);
						lastOffset = i1;
					}
					break;
				case '#':
					if(backslash)
						backslash = false;
					else
					{
						addToken(i - lastOffset,token);
						addToken(length - i,Token.COMMENT1);
						lastOffset = length;
						break loop;
					}
					break;
				case '$':
					if(backslash)
						backslash = false;
					else if(lastOffset != offset)
					{
						addToken(i - lastOffset,token);
						lastOffset = i;
						if(length - i > 1)
						{
							char c1 = array[i1];
							if(c1 == '(' || c1 == '{')
								token = Token.KEYWORD2;
							else
							{
								addToken(2,Token.KEYWORD2);
								lastOffset += 2;
								i++;
							}
						}
					}
					break;
				case '"':
					if(backslash)
						backslash = false;
					else
					{
						addToken(i - lastOffset,token);
						token = Token.LITERAL1;
						lastOffset = i;
					}
					break;
				case '\'':
					if(backslash)
						backslash = false;
					else
					{
						addToken(i - lastOffset,token);
						token = Token.LITERAL2;
						lastOffset = i;
					}
					break;
				default:
					backslash = false;
					break;
				}
			case Token.KEYWORD2:
				backslash = false;
				if(c == ')' || c == '}')
				{
					addToken(i1 - lastOffset,token);
					token = Token.NULL;
					lastOffset = i1;
				}
				break;
			case Token.LITERAL1:
				if(backslash)
					backslash = false;
				else if(c == '"')
				{
					addToken(i1 - lastOffset,token);
					token = Token.NULL;
					lastOffset = i1;
				}
				else
					backslash = false;
				break;
			case Token.LITERAL2:
				if(backslash)
					backslash = false;
				else if(c == '\'')
				{
					addToken(i1 - lastOffset,Token.LITERAL1);
					token = Token.NULL;
					lastOffset = i1;
				}
				else
					backslash = false;
				break;
			}
		}
		switch(token)
		{
		case Token.KEYWORD2:
			addToken(length - lastOffset,Token.INVALID);
			token = Token.NULL;
			break;
		case Token.LITERAL2:
			addToken(length - lastOffset,Token.LITERAL1);
			break;
		default:
			addToken(length - lastOffset,token);
			break;
		}
		return token;
	}
}
