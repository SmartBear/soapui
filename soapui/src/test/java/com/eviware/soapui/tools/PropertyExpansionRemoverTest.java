package com.eviware.soapui.tools;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for PropertyExpansionRemover.
 */
public class PropertyExpansionRemoverTest
{

	@Test
	public void removesPropertyExpansion() throws Exception
	{
		String stringWithPropertyExpansion = "<xsd:attribute name=\"name\" type=\"xsd:string\" default=\"${#Project#MyValue }\"/>";
		assertThat( PropertyExpansionRemover.removeExpansions(stringWithPropertyExpansion ),
				is("<xsd:attribute name=\"name\" type=\"xsd:string\" default=\"\"/>"));
	}

	@Test
	public void removesDynamicPropertyExpansion() throws Exception
	{
		String stringWithDynamicPropertyExpansion = "<xsd:attribute name=\"name\" type=\"xsd:string\" default=\"${= new java.util.Date() }\"/>";
		assertThat( PropertyExpansionRemover.removeExpansions(stringWithDynamicPropertyExpansion ),
				is("<xsd:attribute name=\"name\" type=\"xsd:string\" default=\"\"/>"));
	}

	@Test
	public void removesNestedPropertyExpansion() throws Exception
	{
		String stringWithDynamicPropertyExpansion = "<xsd:attribute name=\"name\" type=\"xsd:string\" default=\"${#testxml#${testxpath}}\"/>";
		assertThat( PropertyExpansionRemover.removeExpansions(stringWithDynamicPropertyExpansion ),
				is("<xsd:attribute name=\"name\" type=\"xsd:string\" default=\"\"/>"));
	}

	@Test
	public void removesMultiplePropertyExpansions() throws Exception
	{
		String stringWithMultiplePropertyExpansions =
				"<!-- ${= 5- + 2}--><xsd:attribute name=\"name\" type=\"xsd:string\" default=\"${#testxml#${testxpath}}\"/>";
		assertThat( PropertyExpansionRemover.removeExpansions(stringWithMultiplePropertyExpansions ),
				is("<!-- --><xsd:attribute name=\"name\" type=\"xsd:string\" default=\"\"/>"));
	}

	@Test
	public void doesNotRemoveSpecialCharactersWhenNotPropertyExpansion() throws Exception
	{
		String stringWithSpecialCharacters = "<xsd:attribute name=\"name\" type=\"xsd:string\" default=\"$ { #testxml#$ {testxpath} } ${\"/>";
		assertThat( PropertyExpansionRemover.removeExpansions(stringWithSpecialCharacters ),
				is(stringWithSpecialCharacters));
	}
}
