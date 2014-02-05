package com.eviware.soapui.tools;

/**
 * Removes property expansions from an input string.
 */
public class PropertyExpansionRemover
{

	public static final String EXPANSION_START = "${";

	public static String removeExpansions( String input )
	{
		String output = input;
		while (containsPropertyExpansion(output))
		{
			output = removeExpansionAt( output, output.indexOf( EXPANSION_START ) );
		}
		return output;
	}

	private static String removeExpansionAt( String input, int startIndex )
	{
		String output = input;
		while (containsNestedExpansion(output, startIndex))
		{
			output = removeExpansionAt( output, output.indexOf( EXPANSION_START, startIndex + 1 ) );
		}
		int endIndex = output.indexOf('}', startIndex);
		return endIndex == -1 ? output : output.substring(0, startIndex) + output.substring(endIndex + 1);
	}

	private static boolean containsNestedExpansion( String output, int startIndex )
	{
		String textToProcess = output.substring(startIndex + EXPANSION_START.length());
		return textToProcess.contains( EXPANSION_START ) &&
				textToProcess.indexOf( EXPANSION_START ) < textToProcess.indexOf( '}' );
	}

	private static boolean containsPropertyExpansion( String input )
	{
		if (input == null || !input.contains( EXPANSION_START ))
		{
			return false;
		}
		int startIndex = input.indexOf( EXPANSION_START );
		return input.indexOf('}', startIndex) != -1;
	}
}
