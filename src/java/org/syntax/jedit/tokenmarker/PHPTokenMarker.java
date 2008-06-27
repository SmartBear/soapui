/*
 * PHPTokenMarker.java - Token marker for PHP
 * Copyright (C) 1999 Clancy Malcolm
 * Based on HTMLTokenMarker.java Copyright (C) 1998, 1999 Slava Pestov
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
 * PHP token marker.
 *
 * @author Clancy Malcolm
 * @version $Id: PHPTokenMarker.java,v 1.1 1999/12/14 04:20:35 sp Exp $
 */
public class PHPTokenMarker extends TokenMarker
{
	public static final byte SCRIPT = Token.INTERNAL_FIRST;

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
			case Token.NULL: // HTML text
				backslash = false;
				switch(c)
				{
				case '<':
					addToken(i - lastOffset,token);
					lastOffset = lastKeyword = i;
					if(SyntaxUtilities.regionMatches(false,
						line,i1,"!--"))
					{
						i += 3;
						token = Token.COMMENT1;
					}
					else if(SyntaxUtilities.regionMatches(
						true,line,i1,"?php"))
					{
						addToken(5,Token.LABEL);
						lastOffset = lastKeyword = (i += 4) + 1;
						token = SCRIPT;
					}
					else if(SyntaxUtilities.regionMatches(
						true,line,i1,"?"))
					{
						addToken(2,Token.LABEL);
						lastOffset = lastKeyword = (i += 1) + 1;
						token = SCRIPT;
					}
					else if(SyntaxUtilities.regionMatches(
						true,line,i1,"script>"))
					{
						addToken(8,Token.LABEL);
						lastOffset = lastKeyword = (i += 7) + 1;
						token = SCRIPT;
					}
					else
					{
						token = Token.KEYWORD1;
					}
					break;
				case '&':
					addToken(i - lastOffset,token);
					lastOffset = lastKeyword = i;
					token = Token.KEYWORD2;
					break;
				}
				break;
			case Token.KEYWORD1: // Inside a tag
				backslash = false;
				if(c == '>')
				{
					addToken(i1 - lastOffset,token);
					lastOffset = lastKeyword = i1;
					token = Token.NULL;
				}
				break;
			case Token.KEYWORD2: // Inside an entity
				backslash = false;
				if(c == ';')
				{
					addToken(i1 - lastOffset,token);
					lastOffset = lastKeyword = i1;
					token = Token.NULL;
					break;
				}
				break;
			case Token.COMMENT1: // Inside a comment
				backslash = false;
				if(SyntaxUtilities.regionMatches(false,line,i,"-->"))
				{
					addToken(i + 3 - lastOffset,token);
					i += 2;
					lastOffset = lastKeyword = i + 1;
					token = Token.NULL;
				}
				break;
			case SCRIPT: // Inside a JavaScript or PHP
				switch(c)
				{
				case '<':
					backslash = false;
					doKeyword(line,i,c);
					if(SyntaxUtilities.regionMatches(true,
						line,i1,"/script>"))
					{
						//Ending the script
						addToken(i - lastOffset,
							Token.KEYWORD3);
						addToken(9,Token.LABEL);
						lastOffset = lastKeyword = (i += 8) + 1;
						token = Token.NULL;
					}
					else
					{
						// < operator
						addToken(i - lastOffset,
							Token.KEYWORD3);
						addToken(1,Token.OPERATOR);
						lastOffset = lastKeyword = i1;
					}
					break;
				case '?':
					backslash = false;
					doKeyword(line, i, c);
					if (array[i1] == '>')
					{
						//Ending the script
						addToken(i - lastOffset,
						Token.KEYWORD3);
						addToken(2,Token.LABEL);
						lastOffset = lastKeyword = (i += 1) + 1;
						token = Token.NULL;
					}
					else
					{
						//? operator
						addToken(i - lastOffset, Token.KEYWORD3);
						addToken(1,Token.OPERATOR);
						lastOffset = lastKeyword = i1;
					}
					break;
				case '"':
					if(backslash)
						backslash = false;
					else
					{
						doKeyword(line,i,c);
						addToken(i - lastOffset,Token.KEYWORD3);
						lastOffset = lastKeyword = i;
						token = Token.LITERAL1;
					}
					break;
				case '\'':
					if(backslash)
						backslash = false;
					else
					{
						doKeyword(line,i,c);
						addToken(i - lastOffset,Token.KEYWORD3);
						lastOffset = lastKeyword = i;
						token = Token.LITERAL2;
					}
					break;
				case '#':
					doKeyword(line,i,c);
					addToken(i - lastOffset,Token.KEYWORD3);
					addToken(length - i,Token.COMMENT2);
					lastOffset = lastKeyword = length;
					break loop;
				case '/':
					backslash = false;
					doKeyword(line,i,c);
					if(length - i > 1)  /*This is the same as if(length > i + 1) */
					{
						addToken(i - lastOffset,Token.KEYWORD3);
						lastOffset = lastKeyword = i;
						if(array[i1] == '/')
						{
							addToken(length - i,Token.COMMENT2);
							lastOffset = lastKeyword = length;
							break loop;
						}
						else if(array[i1] == '*')
						{
							token = Token.COMMENT2;
						}
						else
						{
							// / operator
							addToken(i - lastOffset, Token.KEYWORD3);
							addToken(1,Token.OPERATOR);
							lastOffset = lastKeyword = i1;
						}
	 				}
					else
					{
						// / operator
						addToken(i - lastOffset, Token.KEYWORD3);
						addToken(1,Token.OPERATOR);
						lastOffset = lastKeyword = i1;
					}
					break;
				default:
					backslash = false;
					if(!Character.isLetterOrDigit(c)
						&& c != '_' && c != '$')
					{
						doKeyword(line,i,c);
						if (c != ' ')
						{
							//assume non alphanumeric characters are operators
							addToken(i - lastOffset, Token.KEYWORD3);
							addToken(1,Token.OPERATOR);
							lastOffset = lastKeyword = i1;
						}
					}
					break;
				}
				break;
			case Token.LITERAL1: // Script "..."
				if(backslash)
					backslash = false;
				else if(c == '"')
				{
					addToken(i1 - lastOffset,Token.LITERAL1);
					lastOffset = lastKeyword = i1;
					token = SCRIPT;
				}
				break;
			case Token.LITERAL2: // Script '...'
				if(backslash)
					backslash = false;
				else if(c == '\'')
				{
					addToken(i1 - lastOffset,Token.LITERAL1);
					lastOffset = lastKeyword = i1;
					token = SCRIPT;
				}
				break;
			case Token.COMMENT2: // Inside a Script comment
				backslash = false;
				if(c == '*' && length - i > 1 && array[i1] == '/')
				{
					addToken(i + 2 - lastOffset,Token.COMMENT2);
					i += 1;
					lastOffset = lastKeyword = i + 1;
					token = SCRIPT;
				}
				break;
			default:
				throw new InternalError("Invalid state: "
					+ token);
			}
		}

		switch(token)
		{
		case Token.LITERAL1:
			addToken(length - lastOffset,Token.LITERAL1);
			break;
		case Token.LITERAL2:
			addToken(length - lastOffset,Token.LITERAL2);
			break;
		case Token.KEYWORD2:
			addToken(length - lastOffset,Token.INVALID);
			token = Token.NULL;
			break;
		case SCRIPT:
			doKeyword(line,length,'\0');
			addToken(length - lastOffset,Token.KEYWORD3);
			break;
		default:
			addToken(length - lastOffset,token);
			break;
		}

		return token;
	}

	// private members
	private static KeywordMap keywords;
	private int lastOffset;
	private int lastKeyword;

	static
	{
		keywords = new KeywordMap(false);
		keywords.add("function",Token.KEYWORD2);
		keywords.add("class",Token.KEYWORD2);
		keywords.add("var",Token.KEYWORD2);
		keywords.add("require",Token.KEYWORD2);
		keywords.add("include",Token.KEYWORD2);
		keywords.add("else",Token.KEYWORD1);
		keywords.add("elseif",Token.KEYWORD1);
		keywords.add("do",Token.KEYWORD1);
		keywords.add("for",Token.KEYWORD1);
		keywords.add("if",Token.KEYWORD1);
		keywords.add("endif",Token.KEYWORD1);
		keywords.add("in",Token.KEYWORD1);
		keywords.add("new",Token.KEYWORD1);
		keywords.add("return",Token.KEYWORD1);
		keywords.add("while",Token.KEYWORD1);
		keywords.add("endwhile",Token.KEYWORD1);
		keywords.add("with",Token.KEYWORD1);
		keywords.add("break",Token.KEYWORD1);
		keywords.add("switch",Token.KEYWORD1);
		keywords.add("case",Token.KEYWORD1);
		keywords.add("continue",Token.KEYWORD1);
		keywords.add("default",Token.KEYWORD1);
		keywords.add("echo",Token.KEYWORD1);
		keywords.add("false",Token.KEYWORD1);
		keywords.add("this",Token.KEYWORD1);
		keywords.add("true",Token.KEYWORD1);
		keywords.add("array",Token.KEYWORD1);
		keywords.add("extends",Token.KEYWORD1);
	}

	private boolean doKeyword(Segment line, int i, char c)
	{
		int i1 = i+1;

		int len = i - lastKeyword;
		byte id = keywords.lookup(line,lastKeyword,len);
		if(id != Token.NULL)
		{
			if(lastKeyword != lastOffset)
				addToken(lastKeyword - lastOffset,Token.KEYWORD3);
			addToken(len,id);
			lastOffset = i;
		}
		lastKeyword = i1;
		return false;
	}
}
