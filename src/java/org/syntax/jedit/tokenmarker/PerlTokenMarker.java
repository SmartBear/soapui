/*
 * PerlTokenMarker.java - Perl token marker
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
 * Perl token marker.
 *
 * @author Slava Pestov
 * @version $Id: PerlTokenMarker.java,v 1.11 1999/12/13 03:40:30 sp Exp $
 */
public class PerlTokenMarker extends TokenMarker
{
	// public members
	public static final byte S_ONE = Token.INTERNAL_FIRST;
	public static final byte S_TWO = (byte)(Token.INTERNAL_FIRST + 1);
	public static final byte S_END = (byte)(Token.INTERNAL_FIRST + 2);

	public PerlTokenMarker()
	{
		this(getKeywords());
	}

	public PerlTokenMarker(KeywordMap keywords)
	{
		this.keywords = keywords;
	}

	public byte markTokensImpl(byte _token, Segment line, int lineIndex)
	{
		char[] array = line.array;
		int offset = line.offset;
		token = _token;
		lastOffset = offset;
		lastKeyword = offset;
		matchChar = '\0';
		matchCharBracket = false;
		matchSpacesAllowed = false;
		int length = line.count + offset;

		if(token == Token.LITERAL1 && lineIndex != 0
			&& lineInfo[lineIndex - 1].obj != null)
		{
			String str = (String)lineInfo[lineIndex - 1].obj;
			if(str != null && str.length() == line.count
				&& SyntaxUtilities.regionMatches(false,line,
				offset,str))
			{
				addToken(line.count,token);
				return Token.NULL;
			}
			else
			{
				addToken(line.count,token);
				lineInfo[lineIndex].obj = str;
				return token;
			}
		}

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
					if(doKeyword(line,i,c))
						break;
					if(backslash)
						backslash = false;
					else
					{
						addToken(i - lastOffset,token);
						addToken(length - i,Token.COMMENT1);
						lastOffset = lastKeyword = length;
						break loop;
					}
					break;
				case '=':
					backslash = false;
					if(i == offset)
					{
						token = Token.COMMENT2;
						addToken(length - i,token);
						lastOffset = lastKeyword = length;
						break loop;
					}
					else
						doKeyword(line,i,c);
					break;
				case '$': case '&': case '%': case '@':
					backslash = false;
					if(doKeyword(line,i,c))
						break;
					if(length - i > 1)
					{
						if(c == '&' && (array[i1] == '&'
							|| Character.isWhitespace(
							array[i1])))
							i++;
						else
						{
							addToken(i - lastOffset,token);
							lastOffset = lastKeyword = i;
							token = Token.KEYWORD2;
						}
					}
					break;
				case '"':
					if(doKeyword(line,i,c))
						break;
					if(backslash)
						backslash = false;
					else
					{
						addToken(i - lastOffset,token);
						token = Token.LITERAL1;
						lineInfo[lineIndex].obj = null;
						lastOffset = lastKeyword = i;
					}
					break;
				case '\'':
					if(backslash)
						backslash = false;
					else
					{
						int oldLastKeyword = lastKeyword;
						if(doKeyword(line,i,c))
							break;
						if(i != oldLastKeyword)
							break;
						addToken(i - lastOffset,token);
						token = Token.LITERAL2;
						lastOffset = lastKeyword = i;
					}
					break;
				case '`':
					if(doKeyword(line,i,c))
						break;
					if(backslash)
						backslash = false;
					else
					{
						addToken(i - lastOffset,token);
						token = Token.OPERATOR;
						lastOffset = lastKeyword = i;
					}
					break;
				case '<':
					if(doKeyword(line,i,c))
						break;
					if(backslash)
						backslash = false;
					else
					{
						if(length - i > 2 && array[i1] == '<'
							&& !Character.isWhitespace(array[i+2]))
						{
							addToken(i - lastOffset,token);
							lastOffset = lastKeyword = i;
							token = Token.LITERAL1;
							int len = length - (i+2);
							if(array[length - 1] == ';')
								len--;
							lineInfo[lineIndex].obj =
								createReadinString(array,i + 2,len);
						}
					}
					break;
				case ':':
					backslash = false;
					if(doKeyword(line,i,c))
						break;
					// Doesn't pick up all labels,
					// but at least doesn't mess up
					if(lastKeyword != 0)
						break;
					addToken(i1 - lastOffset,Token.LABEL);
					lastOffset = lastKeyword = i1;
					break;
				case '-':
					backslash = false;
					if(doKeyword(line,i,c))
						break;
					if(i != lastKeyword || length - i <= 1)
						break;
					switch(array[i1])
					{
					case 'r': case 'w': case 'x':
					case 'o': case 'R': case 'W':
					case 'X': case 'O': case 'e':
					case 'z': case 's': case 'f':
					case 'd': case 'l': case 'p':
					case 'S': case 'b': case 'c':
					case 't': case 'u': case 'g':
					case 'k': case 'T': case 'B':
					case 'M': case 'A': case 'C':
						addToken(i - lastOffset,token);
						addToken(2,Token.KEYWORD3);
						lastOffset = lastKeyword = i+2;
						i++;
					}
					break;
				case '/': case '?':
					if(doKeyword(line,i,c))
						break;
					if(length - i > 1)
					{
						backslash = false;
						char ch = array[i1];
						if(Character.isWhitespace(ch))
							break;
						matchChar = c;
						matchSpacesAllowed = false;
						addToken(i - lastOffset,token);
						token = S_ONE;
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
			case Token.KEYWORD2:
				backslash = false;
				// This test checks for an end-of-variable
				// condition
				if(!Character.isLetterOrDigit(c) && c != '_'
					&& c != '#' && c != '\'' && c != ':'
					&& c != '&')
				{
					// If this is the first character
					// of the variable name ($'aaa)
					// ignore it
					if(i != offset && array[i-1] == '$')
					{
						addToken(i1 - lastOffset,token);
						lastOffset = lastKeyword = i1;
					}
					// Otherwise, end of variable...
					else
					{
						addToken(i - lastOffset,token);
						lastOffset = lastKeyword = i;
						// Wind back so that stuff
						// like $hello$fred is picked
						// up
						i--;
						token = Token.NULL;
					}
				}
				break;
			case S_ONE: case S_TWO:
				if(backslash)
					backslash = false;
				else
				{
					if(matchChar == '\0')
					{
						if(Character.isWhitespace(matchChar)
							&& !matchSpacesAllowed)
							break;
						else
							matchChar = c;
					}
					else
					{
						switch(matchChar)
						{
						case '(':
							matchChar = ')';
							matchCharBracket = true;
							break;
						case '[':
							matchChar = ']';
							matchCharBracket = true;
							break;
						case '{':
							matchChar = '}';
							matchCharBracket = true;
							break;
						case '<':
							matchChar = '>';
							matchCharBracket = true;
							break;
						default:
							matchCharBracket = false;
							break;
						}
						if(c != matchChar)
							break;
						if(token == S_TWO)
						{
							token = S_ONE;
							if(matchCharBracket)
								matchChar = '\0';
						}
						else
						{
							token = S_END;
							addToken(i1 - lastOffset,
								Token.LITERAL2);
							lastOffset = lastKeyword = i1;
						}
					}
				}
				break;
			case S_END:
				backslash = false;
				if(!Character.isLetterOrDigit(c)
					&& c != '_')
					doKeyword(line,i,c);
				break;
			case Token.COMMENT2:
				backslash = false;
				if(i == offset)
				{
					addToken(line.count,token);
					if(length - i > 3 && SyntaxUtilities
						.regionMatches(false,line,offset,"=cut"))
						token = Token.NULL;
					lastOffset = lastKeyword = length;
					break loop;
				}
				break;
			case Token.LITERAL1:
				if(backslash)
					backslash = false;
				/* else if(c == '$')
					backslash = true; */
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
				/* else if(c == '$')
					backslash = true; */
				else if(c == '\'')
				{
					addToken(i1 - lastOffset,Token.LITERAL1);
					token = Token.NULL;
					lastOffset = lastKeyword = i1;
				}
				break;
			case Token.OPERATOR:
				if(backslash)
					backslash = false;
				else if(c == '`')
				{
					addToken(i1 - lastOffset,token);
					token = Token.NULL;
					lastOffset = lastKeyword = i1;
				}
				break;
			default:
				throw new InternalError("Invalid state: "
					+ token);
			}
		}

		if(token == Token.NULL)
			doKeyword(line,length,'\0');

		switch(token)
		{
		case Token.KEYWORD2:
			addToken(length - lastOffset,token);
			token = Token.NULL;
			break;
		case Token.LITERAL2:
			addToken(length - lastOffset,Token.LITERAL1);
			break;
		case S_END:
			addToken(length - lastOffset,Token.LITERAL2);
			token = Token.NULL;
			break;
		case S_ONE: case S_TWO:
			addToken(length - lastOffset,Token.INVALID);
			token = Token.NULL;
			break;
		default:
			addToken(length - lastOffset,token);
			break;
		}
		return token;
	}

	// private members
	private KeywordMap keywords;
	private byte token;
	private int lastOffset;
	private int lastKeyword;
	private char matchChar;
	private boolean matchCharBracket;
	private boolean matchSpacesAllowed;

	private boolean doKeyword(Segment line, int i, char c)
	{
		int i1 = i+1;

		if(token == S_END)
		{
			addToken(i - lastOffset,Token.LITERAL2);
			token = Token.NULL;
			lastOffset = i;
			lastKeyword = i1;
			return false;
		}

		int len = i - lastKeyword;
		byte id = keywords.lookup(line,lastKeyword,len);
		if(id == S_ONE || id == S_TWO)
		{
			if(lastKeyword != lastOffset)
				addToken(lastKeyword - lastOffset,Token.NULL);
			addToken(len,Token.LITERAL2);
			lastOffset = i;
			lastKeyword = i1;
			if(Character.isWhitespace(c))
				matchChar = '\0';
			else
				matchChar = c;
			matchSpacesAllowed = true;
			token = id;
			return true;
		}
		else if(id != Token.NULL)
		{
			if(lastKeyword != lastOffset)
				addToken(lastKeyword - lastOffset,Token.NULL);
			addToken(len,id);
			lastOffset = i;
		}
		lastKeyword = i1;
		return false;
	}

	// Converts < EOF >, < 'EOF' >, etc to <EOF>
	private String createReadinString(char[] array, int start, int len)
	{
		int idx1 = start;
		int idx2 = start + len - 1;

		while((idx1 <= idx2) && (!Character.isLetterOrDigit(array[idx1])))
			idx1++;

		while((idx1 <= idx2) && (!Character.isLetterOrDigit(array[idx2])))
			idx2--;

		return new String(array, idx1, idx2 - idx1 + 1);
	}

	private static KeywordMap perlKeywords;

	private static KeywordMap getKeywords()
	{
		if(perlKeywords == null)
		{
			perlKeywords = new KeywordMap(false);
			perlKeywords.add("my",Token.KEYWORD1);
			perlKeywords.add("local",Token.KEYWORD1);
			perlKeywords.add("new",Token.KEYWORD1);
			perlKeywords.add("if",Token.KEYWORD1);
			perlKeywords.add("until",Token.KEYWORD1);
			perlKeywords.add("while",Token.KEYWORD1);
			perlKeywords.add("elsif",Token.KEYWORD1);
			perlKeywords.add("else",Token.KEYWORD1);
			perlKeywords.add("eval",Token.KEYWORD1);
			perlKeywords.add("unless",Token.KEYWORD1);
			perlKeywords.add("foreach",Token.KEYWORD1);
			perlKeywords.add("continue",Token.KEYWORD1);
			perlKeywords.add("exit",Token.KEYWORD1);
			perlKeywords.add("die",Token.KEYWORD1);
			perlKeywords.add("last",Token.KEYWORD1);
			perlKeywords.add("goto",Token.KEYWORD1);
			perlKeywords.add("next",Token.KEYWORD1);
			perlKeywords.add("redo",Token.KEYWORD1);
			perlKeywords.add("goto",Token.KEYWORD1);
			perlKeywords.add("return",Token.KEYWORD1);
			perlKeywords.add("do",Token.KEYWORD1);
			perlKeywords.add("sub",Token.KEYWORD1);
			perlKeywords.add("use",Token.KEYWORD1);
			perlKeywords.add("require",Token.KEYWORD1);
			perlKeywords.add("package",Token.KEYWORD1);
			perlKeywords.add("BEGIN",Token.KEYWORD1);
			perlKeywords.add("END",Token.KEYWORD1);
			perlKeywords.add("eq",Token.OPERATOR);
			perlKeywords.add("ne",Token.OPERATOR);
			perlKeywords.add("not",Token.OPERATOR);
			perlKeywords.add("and",Token.OPERATOR);
			perlKeywords.add("or",Token.OPERATOR);

			perlKeywords.add("abs",Token.KEYWORD3);
			perlKeywords.add("accept",Token.KEYWORD3);
			perlKeywords.add("alarm",Token.KEYWORD3);
			perlKeywords.add("atan2",Token.KEYWORD3);
			perlKeywords.add("bind",Token.KEYWORD3);
			perlKeywords.add("binmode",Token.KEYWORD3);
			perlKeywords.add("bless",Token.KEYWORD3);
			perlKeywords.add("caller",Token.KEYWORD3);
			perlKeywords.add("chdir",Token.KEYWORD3);
			perlKeywords.add("chmod",Token.KEYWORD3);
			perlKeywords.add("chomp",Token.KEYWORD3);
			perlKeywords.add("chr",Token.KEYWORD3);
			perlKeywords.add("chroot",Token.KEYWORD3);
			perlKeywords.add("chown",Token.KEYWORD3);
			perlKeywords.add("closedir",Token.KEYWORD3);
			perlKeywords.add("close",Token.KEYWORD3);
			perlKeywords.add("connect",Token.KEYWORD3);
			perlKeywords.add("cos",Token.KEYWORD3);
			perlKeywords.add("crypt",Token.KEYWORD3);
			perlKeywords.add("dbmclose",Token.KEYWORD3);
			perlKeywords.add("dbmopen",Token.KEYWORD3);
			perlKeywords.add("defined",Token.KEYWORD3);
			perlKeywords.add("delete",Token.KEYWORD3);
			perlKeywords.add("die",Token.KEYWORD3);
			perlKeywords.add("dump",Token.KEYWORD3);
			perlKeywords.add("each",Token.KEYWORD3);
			perlKeywords.add("endgrent",Token.KEYWORD3);
			perlKeywords.add("endhostent",Token.KEYWORD3);
			perlKeywords.add("endnetent",Token.KEYWORD3);
			perlKeywords.add("endprotoent",Token.KEYWORD3);
			perlKeywords.add("endpwent",Token.KEYWORD3);
			perlKeywords.add("endservent",Token.KEYWORD3);
			perlKeywords.add("eof",Token.KEYWORD3);
			perlKeywords.add("exec",Token.KEYWORD3);
			perlKeywords.add("exists",Token.KEYWORD3);
			perlKeywords.add("exp",Token.KEYWORD3);
			perlKeywords.add("fctnl",Token.KEYWORD3);
			perlKeywords.add("fileno",Token.KEYWORD3);
			perlKeywords.add("flock",Token.KEYWORD3);
			perlKeywords.add("fork",Token.KEYWORD3);
			perlKeywords.add("format",Token.KEYWORD3);
			perlKeywords.add("formline",Token.KEYWORD3);
			perlKeywords.add("getc",Token.KEYWORD3);
			perlKeywords.add("getgrent",Token.KEYWORD3);
			perlKeywords.add("getgrgid",Token.KEYWORD3);
			perlKeywords.add("getgrnam",Token.KEYWORD3);
			perlKeywords.add("gethostbyaddr",Token.KEYWORD3);
			perlKeywords.add("gethostbyname",Token.KEYWORD3);
			perlKeywords.add("gethostent",Token.KEYWORD3);
			perlKeywords.add("getlogin",Token.KEYWORD3);
			perlKeywords.add("getnetbyaddr",Token.KEYWORD3);
			perlKeywords.add("getnetbyname",Token.KEYWORD3);
			perlKeywords.add("getnetent",Token.KEYWORD3);
			perlKeywords.add("getpeername",Token.KEYWORD3);
			perlKeywords.add("getpgrp",Token.KEYWORD3);
			perlKeywords.add("getppid",Token.KEYWORD3);
			perlKeywords.add("getpriority",Token.KEYWORD3);
			perlKeywords.add("getprotobyname",Token.KEYWORD3);
			perlKeywords.add("getprotobynumber",Token.KEYWORD3);
			perlKeywords.add("getprotoent",Token.KEYWORD3);
			perlKeywords.add("getpwent",Token.KEYWORD3);
			perlKeywords.add("getpwnam",Token.KEYWORD3);
			perlKeywords.add("getpwuid",Token.KEYWORD3);
			perlKeywords.add("getservbyname",Token.KEYWORD3);
			perlKeywords.add("getservbyport",Token.KEYWORD3);
			perlKeywords.add("getservent",Token.KEYWORD3);
			perlKeywords.add("getsockname",Token.KEYWORD3);
			perlKeywords.add("getsockopt",Token.KEYWORD3);
			perlKeywords.add("glob",Token.KEYWORD3);
			perlKeywords.add("gmtime",Token.KEYWORD3);
			perlKeywords.add("grep",Token.KEYWORD3);
			perlKeywords.add("hex",Token.KEYWORD3);
			perlKeywords.add("import",Token.KEYWORD3);
			perlKeywords.add("index",Token.KEYWORD3);
			perlKeywords.add("int",Token.KEYWORD3);
			perlKeywords.add("ioctl",Token.KEYWORD3);
			perlKeywords.add("join",Token.KEYWORD3);
			perlKeywords.add("keys",Token.KEYWORD3);
			perlKeywords.add("kill",Token.KEYWORD3);
			perlKeywords.add("lcfirst",Token.KEYWORD3);
			perlKeywords.add("lc",Token.KEYWORD3);
			perlKeywords.add("length",Token.KEYWORD3);
			perlKeywords.add("link",Token.KEYWORD3);
			perlKeywords.add("listen",Token.KEYWORD3);
			perlKeywords.add("log",Token.KEYWORD3);
			perlKeywords.add("localtime",Token.KEYWORD3);
			perlKeywords.add("lstat",Token.KEYWORD3);
			perlKeywords.add("map",Token.KEYWORD3);
			perlKeywords.add("mkdir",Token.KEYWORD3);
			perlKeywords.add("msgctl",Token.KEYWORD3);
			perlKeywords.add("msgget",Token.KEYWORD3);
			perlKeywords.add("msgrcv",Token.KEYWORD3);
			perlKeywords.add("no",Token.KEYWORD3);
			perlKeywords.add("oct",Token.KEYWORD3);
			perlKeywords.add("opendir",Token.KEYWORD3);
			perlKeywords.add("open",Token.KEYWORD3);
			perlKeywords.add("ord",Token.KEYWORD3);
			perlKeywords.add("pack",Token.KEYWORD3);
			perlKeywords.add("pipe",Token.KEYWORD3);
			perlKeywords.add("pop",Token.KEYWORD3);
			perlKeywords.add("pos",Token.KEYWORD3);
			perlKeywords.add("printf",Token.KEYWORD3);
			perlKeywords.add("print",Token.KEYWORD3);
			perlKeywords.add("push",Token.KEYWORD3);
			perlKeywords.add("quotemeta",Token.KEYWORD3);
			perlKeywords.add("rand",Token.KEYWORD3);
			perlKeywords.add("readdir",Token.KEYWORD3);
			perlKeywords.add("read",Token.KEYWORD3);
			perlKeywords.add("readlink",Token.KEYWORD3);
			perlKeywords.add("recv",Token.KEYWORD3);
			perlKeywords.add("ref",Token.KEYWORD3);
			perlKeywords.add("rename",Token.KEYWORD3);
			perlKeywords.add("reset",Token.KEYWORD3);
			perlKeywords.add("reverse",Token.KEYWORD3);
			perlKeywords.add("rewinddir",Token.KEYWORD3);
			perlKeywords.add("rindex",Token.KEYWORD3);
			perlKeywords.add("rmdir",Token.KEYWORD3);
			perlKeywords.add("scalar",Token.KEYWORD3);
			perlKeywords.add("seekdir",Token.KEYWORD3);
			perlKeywords.add("seek",Token.KEYWORD3);
			perlKeywords.add("select",Token.KEYWORD3);
			perlKeywords.add("semctl",Token.KEYWORD3);
			perlKeywords.add("semget",Token.KEYWORD3);
			perlKeywords.add("semop",Token.KEYWORD3);
			perlKeywords.add("send",Token.KEYWORD3);
			perlKeywords.add("setgrent",Token.KEYWORD3);
			perlKeywords.add("sethostent",Token.KEYWORD3);
			perlKeywords.add("setnetent",Token.KEYWORD3);
			perlKeywords.add("setpgrp",Token.KEYWORD3);
			perlKeywords.add("setpriority",Token.KEYWORD3);
			perlKeywords.add("setprotoent",Token.KEYWORD3);
			perlKeywords.add("setpwent",Token.KEYWORD3);
			perlKeywords.add("setsockopt",Token.KEYWORD3);
			perlKeywords.add("shift",Token.KEYWORD3);
			perlKeywords.add("shmctl",Token.KEYWORD3);
			perlKeywords.add("shmget",Token.KEYWORD3);
			perlKeywords.add("shmread",Token.KEYWORD3);
			perlKeywords.add("shmwrite",Token.KEYWORD3);
			perlKeywords.add("shutdown",Token.KEYWORD3);
			perlKeywords.add("sin",Token.KEYWORD3);
			perlKeywords.add("sleep",Token.KEYWORD3);
			perlKeywords.add("socket",Token.KEYWORD3);
			perlKeywords.add("socketpair",Token.KEYWORD3);
			perlKeywords.add("sort",Token.KEYWORD3);
			perlKeywords.add("splice",Token.KEYWORD3);
			perlKeywords.add("split",Token.KEYWORD3);
			perlKeywords.add("sprintf",Token.KEYWORD3);
			perlKeywords.add("sqrt",Token.KEYWORD3);
			perlKeywords.add("srand",Token.KEYWORD3);
			perlKeywords.add("stat",Token.KEYWORD3);
			perlKeywords.add("study",Token.KEYWORD3);
			perlKeywords.add("substr",Token.KEYWORD3);
			perlKeywords.add("symlink",Token.KEYWORD3);
			perlKeywords.add("syscall",Token.KEYWORD3);
			perlKeywords.add("sysopen",Token.KEYWORD3);
			perlKeywords.add("sysread",Token.KEYWORD3);
			perlKeywords.add("syswrite",Token.KEYWORD3);
			perlKeywords.add("telldir",Token.KEYWORD3);
			perlKeywords.add("tell",Token.KEYWORD3);
			perlKeywords.add("tie",Token.KEYWORD3);
			perlKeywords.add("tied",Token.KEYWORD3);
			perlKeywords.add("time",Token.KEYWORD3);
			perlKeywords.add("times",Token.KEYWORD3);
			perlKeywords.add("truncate",Token.KEYWORD3);
			perlKeywords.add("uc",Token.KEYWORD3);
			perlKeywords.add("ucfirst",Token.KEYWORD3);
			perlKeywords.add("umask",Token.KEYWORD3);
			perlKeywords.add("undef",Token.KEYWORD3);
			perlKeywords.add("unlink",Token.KEYWORD3);
			perlKeywords.add("unpack",Token.KEYWORD3);
			perlKeywords.add("unshift",Token.KEYWORD3);
			perlKeywords.add("untie",Token.KEYWORD3);
			perlKeywords.add("utime",Token.KEYWORD3);
			perlKeywords.add("values",Token.KEYWORD3);
			perlKeywords.add("vec",Token.KEYWORD3);
			perlKeywords.add("wait",Token.KEYWORD3);
			perlKeywords.add("waitpid",Token.KEYWORD3);
			perlKeywords.add("wantarray",Token.KEYWORD3);
			perlKeywords.add("warn",Token.KEYWORD3);
			perlKeywords.add("write",Token.KEYWORD3);

			perlKeywords.add("m",S_ONE);
			perlKeywords.add("q",S_ONE);
			perlKeywords.add("qq",S_ONE);
			perlKeywords.add("qw",S_ONE);
			perlKeywords.add("qx",S_ONE);
			perlKeywords.add("s",S_TWO);
			perlKeywords.add("tr",S_TWO);
			perlKeywords.add("y",S_TWO);
		}
		return perlKeywords;
	}	
}
