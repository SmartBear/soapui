package org.syntax.jedit.tokenmarker;

import org.syntax.jedit.KeywordMap;

public class GroovyTokenMarker extends CTokenMarker
{
	public GroovyTokenMarker()
	{
		super( false, getKeywords() );
	}

	public static KeywordMap getKeywords()
	{
		KeywordMap groovyKeywords = new KeywordMap( false );
		groovyKeywords.add( "as", Token.KEYWORD1 );
		groovyKeywords.add( "assert", Token.KEYWORD1 );
		groovyKeywords.add( "break", Token.KEYWORD1 );
		groovyKeywords.add( "case", Token.KEYWORD1 );
		groovyKeywords.add( "catch", Token.KEYWORD1 );
		groovyKeywords.add( "class", Token.KEYWORD1 );
		groovyKeywords.add( "continue", Token.KEYWORD1 );
		groovyKeywords.add( "def", Token.KEYWORD1 );
		groovyKeywords.add( "default", Token.KEYWORD1 );
		groovyKeywords.add( "do", Token.KEYWORD1 );
		groovyKeywords.add( "else", Token.KEYWORD1 );
		groovyKeywords.add( "extends", Token.KEYWORD1 );
		groovyKeywords.add( "finally", Token.KEYWORD1 );
		groovyKeywords.add( "for", Token.KEYWORD1 );
		groovyKeywords.add( "if", Token.KEYWORD1 );
		groovyKeywords.add( "in", Token.KEYWORD1 );
		groovyKeywords.add( "implements", Token.KEYWORD1 );
		groovyKeywords.add( "import", Token.KEYWORD1 );
		groovyKeywords.add( "instanceof", Token.KEYWORD1 );
		groovyKeywords.add( "interface", Token.KEYWORD1 );
		groovyKeywords.add( "new", Token.KEYWORD1 );
		groovyKeywords.add( "package", Token.KEYWORD1 );
		groovyKeywords.add( "property", Token.KEYWORD1 );
		groovyKeywords.add( "return", Token.KEYWORD1 );
		groovyKeywords.add( "switch", Token.KEYWORD1 );
		groovyKeywords.add( "throw", Token.KEYWORD1 );
		groovyKeywords.add( "throws", Token.KEYWORD1 );
		groovyKeywords.add( "try", Token.KEYWORD1 );
		groovyKeywords.add( "while", Token.KEYWORD1 );

		return groovyKeywords;
	}

	// private members
	// private static KeywordMap groovyKeywords;
}
