/*
 * PythonTokenMarker.java - Python token marker
 * Copyright (C) 1999 Jonathan Revusky
 * Copyright (C) 1998, 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package org.syntax.jedit.tokenmarker;

import javax.swing.text.Segment;

import org.syntax.jedit.KeywordMap;
import org.syntax.jedit.SyntaxUtilities;

/**
 * Python token marker.
 *
 * @author Jonathan Revusky
 * @version $Id: PythonTokenMarker.java,v 1.3 1999/12/14 04:20:35 sp Exp $
 */
public class PythonTokenMarker extends TokenMarker
{
	private static final byte TRIPLEQUOTE1 = Token.INTERNAL_FIRST;
	private static final byte TRIPLEQUOTE2 = Token.INTERNAL_LAST;

	public PythonTokenMarker()
	{
		this.keywords = getKeywords();
	}

	public byte markTokensImpl(byte token, Segment line, int lineIndex)
	{
		char[] array = line.array;
		int offset = line.offset;
		lastOffset = offset;
		lastKeyword = offset;
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
				case '#':
					if(backslash)
						backslash = false;
					else
					{
						doKeyword(line,i,c);
						addToken(i - lastOffset,token);
						addToken(length - i,Token.COMMENT1);
						lastOffset = lastKeyword = length;
						break loop;
					}
					break;
				case '"':
					doKeyword(line,i,c);
					if(backslash)
						backslash = false;
					else
					{
						addToken(i - lastOffset,token);
						if(SyntaxUtilities.regionMatches(false,
							line,i1,"\"\""))
						{
							token = TRIPLEQUOTE1;
						}
						else
						{
							token = Token.LITERAL1;
						}
						lastOffset = lastKeyword = i;
					}
					break;
				case '\'':
					doKeyword(line,i,c);
					if(backslash)
						backslash = false;
					else
					{
						addToken(i - lastOffset,token);
						if(SyntaxUtilities.regionMatches(false,
							line,i1,"''"))
						{
							token = TRIPLEQUOTE2;
						}
						else
						{
							token = Token.LITERAL2;
						}
						lastOffset = lastKeyword = i;
					}
					break;
				default:
					backslash = false;
					if(!Character.isLetterOrDigit(c)
						&& c != '_')
						doKeyword(line,i,c);
					break;
				}
				break;
			case Token.LITERAL1:
				if(backslash)
					backslash = false;
				else if(c == '"')
				{
					addToken(i1 - lastOffset,token);
					token = Token.NULL;
					lastOffset = lastKeyword = i1;
				}
				break;
			case Token.LITERAL2:
				if(backslash)
					backslash = false;
				else if(c == '\'')
				{
					addToken(i1 - lastOffset,Token.LITERAL1);
					token = Token.NULL;
					lastOffset = lastKeyword = i1;
				}
				break;
			case TRIPLEQUOTE1:
				if(backslash)
					backslash = false;
				else if(SyntaxUtilities.regionMatches(false,
					line,i,"\"\"\""))
				{
					addToken((i+=4) - lastOffset,
						Token.LITERAL1);
					token = Token.NULL;
					lastOffset = lastKeyword = i;
				}
				break;
			case TRIPLEQUOTE2:
				if(backslash)
					backslash = false;
				else if(SyntaxUtilities.regionMatches(false,
					line,i,"'''"))
				{
					addToken((i+=4) - lastOffset,
						Token.LITERAL1);
					token = Token.NULL;
					lastOffset = lastKeyword = i;
				}
				break;
			default:
				throw new InternalError("Invalid state: "
					+ token);
			}
		}

		switch(token)
		{
			case TRIPLEQUOTE1:
			case TRIPLEQUOTE2:
				addToken(length - lastOffset,Token.LITERAL1);
				break;
			case Token.NULL:
				doKeyword(line,length,'\0');
			default:
				addToken(length - lastOffset,token);
				break;
		}

		return token;
	}

	public static KeywordMap getKeywords()
	{
		if (pyKeywords == null)
		{
			pyKeywords = new KeywordMap(false);
			pyKeywords.add("and",Token.KEYWORD3);
			pyKeywords.add("not",Token.KEYWORD3);
			pyKeywords.add("or",Token.KEYWORD3);
			pyKeywords.add("if",Token.KEYWORD1);
			pyKeywords.add("for",Token.KEYWORD1);
			pyKeywords.add("assert",Token.KEYWORD1);
			pyKeywords.add("break",Token.KEYWORD1);
			pyKeywords.add("continue",Token.KEYWORD1);
			pyKeywords.add("elif",Token.KEYWORD1);
			pyKeywords.add("else",Token.KEYWORD1);
			pyKeywords.add("except",Token.KEYWORD1);
			pyKeywords.add("exec",Token.KEYWORD1);
			pyKeywords.add("finally",Token.KEYWORD1);
			pyKeywords.add("raise",Token.KEYWORD1);
			pyKeywords.add("return",Token.KEYWORD1);
			pyKeywords.add("try",Token.KEYWORD1);
			pyKeywords.add("while",Token.KEYWORD1);
			pyKeywords.add("def",Token.KEYWORD2);
			pyKeywords.add("class",Token.KEYWORD2);
			pyKeywords.add("del",Token.KEYWORD2);
			pyKeywords.add("from",Token.KEYWORD2);
			pyKeywords.add("global",Token.KEYWORD2);
			pyKeywords.add("import",Token.KEYWORD2);
			pyKeywords.add("in",Token.KEYWORD2);
			pyKeywords.add("is",Token.KEYWORD2);
			pyKeywords.add("lambda",Token.KEYWORD2);
			pyKeywords.add("pass",Token.KEYWORD2);
			pyKeywords.add("print",Token.KEYWORD2);
		}
		return pyKeywords;
	}

	// private members
	private static KeywordMap pyKeywords;

	private KeywordMap keywords;
	private int lastOffset;
	private int lastKeyword;

	private boolean doKeyword(Segment line, int i, char c)
	{
		int i1 = i+1;

		int len = i - lastKeyword;
		byte id = keywords.lookup(line,lastKeyword,len);
		if(id != Token.NULL)
		{
			if(lastKeyword != lastOffset)
				addToken(lastKeyword - lastOffset,Token.NULL);
			addToken(len,id);
			lastOffset = i;
		}
		lastKeyword = i1;
		return false;
	}
}
