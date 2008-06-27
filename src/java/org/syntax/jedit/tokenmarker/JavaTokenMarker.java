/*
 * JavaTokenMarker.java - Java token marker
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package org.syntax.jedit.tokenmarker;

import org.syntax.jedit.KeywordMap;

/**
 * Java token marker.
 *
 * @author Slava Pestov
 * @version $Id: JavaTokenMarker.java,v 1.5 1999/12/13 03:40:30 sp Exp $
 */
public class JavaTokenMarker extends CTokenMarker
{
	public JavaTokenMarker()
	{
		super(false,getKeywords());
	}

	public static KeywordMap getKeywords()
	{
		if(javaKeywords == null)
		{
			javaKeywords = new KeywordMap(false);
			javaKeywords.add("package",Token.KEYWORD2);
			javaKeywords.add("import",Token.KEYWORD2);
			javaKeywords.add("byte",Token.KEYWORD3);
			javaKeywords.add("char",Token.KEYWORD3);
			javaKeywords.add("short",Token.KEYWORD3);
			javaKeywords.add("int",Token.KEYWORD3);
			javaKeywords.add("long",Token.KEYWORD3);
			javaKeywords.add("float",Token.KEYWORD3);
			javaKeywords.add("double",Token.KEYWORD3);
			javaKeywords.add("boolean",Token.KEYWORD3);
			javaKeywords.add("void",Token.KEYWORD3);
			javaKeywords.add("class",Token.KEYWORD3);
			javaKeywords.add("interface",Token.KEYWORD3);
			javaKeywords.add("abstract",Token.KEYWORD1);
			javaKeywords.add("final",Token.KEYWORD1);
			javaKeywords.add("private",Token.KEYWORD1);
			javaKeywords.add("protected",Token.KEYWORD1);
			javaKeywords.add("public",Token.KEYWORD1);
			javaKeywords.add("static",Token.KEYWORD1);
			javaKeywords.add("synchronized",Token.KEYWORD1);
			javaKeywords.add("native",Token.KEYWORD1);
			javaKeywords.add("volatile",Token.KEYWORD1);
			javaKeywords.add("transient",Token.KEYWORD1);
			javaKeywords.add("break",Token.KEYWORD1);
			javaKeywords.add("case",Token.KEYWORD1);
			javaKeywords.add("continue",Token.KEYWORD1);
			javaKeywords.add("default",Token.KEYWORD1);
			javaKeywords.add("do",Token.KEYWORD1);
			javaKeywords.add("else",Token.KEYWORD1);
			javaKeywords.add("for",Token.KEYWORD1);
			javaKeywords.add("if",Token.KEYWORD1);
			javaKeywords.add("instanceof",Token.KEYWORD1);
			javaKeywords.add("new",Token.KEYWORD1);
			javaKeywords.add("return",Token.KEYWORD1);
			javaKeywords.add("switch",Token.KEYWORD1);
			javaKeywords.add("while",Token.KEYWORD1);
			javaKeywords.add("throw",Token.KEYWORD1);
			javaKeywords.add("try",Token.KEYWORD1);
			javaKeywords.add("catch",Token.KEYWORD1);
			javaKeywords.add("extends",Token.KEYWORD1);
			javaKeywords.add("finally",Token.KEYWORD1);
			javaKeywords.add("implements",Token.KEYWORD1);
			javaKeywords.add("throws",Token.KEYWORD1);
			javaKeywords.add("this",Token.LITERAL2);
			javaKeywords.add("null",Token.LITERAL2);
			javaKeywords.add("super",Token.LITERAL2);
			javaKeywords.add("true",Token.LITERAL2);
			javaKeywords.add("false",Token.LITERAL2);
		}
		return javaKeywords;
	}

	// private members
	private static KeywordMap javaKeywords;
}
