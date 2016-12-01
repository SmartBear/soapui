/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.tools;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for PropertyExpansionRemover.
 */
public class PropertyExpansionRemoverTest {

    @Test
    public void removesPropertyExpansion() throws Exception {
        String stringWithPropertyExpansion = "<xsd:attribute name=\"name\" type=\"xsd:string\" default=\"${#Project#MyValue }\"/>";
        assertThat(PropertyExpansionRemover.removeExpansions(stringWithPropertyExpansion),
                is("<xsd:attribute name=\"name\" type=\"xsd:string\" default=\"\"/>"));
    }

    @Test
    public void removesDynamicPropertyExpansion() throws Exception {
        String stringWithDynamicPropertyExpansion = "<xsd:attribute name=\"name\" type=\"xsd:string\" default=\"${= new java.util.Date() }\"/>";
        assertThat(PropertyExpansionRemover.removeExpansions(stringWithDynamicPropertyExpansion),
                is("<xsd:attribute name=\"name\" type=\"xsd:string\" default=\"\"/>"));
    }

    @Test
    public void removesNestedPropertyExpansion() throws Exception {
        String stringWithDynamicPropertyExpansion = "<xsd:attribute name=\"name\" type=\"xsd:string\" default=\"${#testxml#${testxpath}}\"/>";
        assertThat(PropertyExpansionRemover.removeExpansions(stringWithDynamicPropertyExpansion),
                is("<xsd:attribute name=\"name\" type=\"xsd:string\" default=\"\"/>"));
    }

    @Test
    public void removesMultiplePropertyExpansions() throws Exception {
        String stringWithMultiplePropertyExpansions =
                "<!-- ${= 5- + 2}--><xsd:attribute name=\"name\" type=\"xsd:string\" default=\"${#testxml#${testxpath}}\"/>";
        assertThat(PropertyExpansionRemover.removeExpansions(stringWithMultiplePropertyExpansions),
                is("<!-- --><xsd:attribute name=\"name\" type=\"xsd:string\" default=\"\"/>"));
    }

    @Test
    public void doesNotRemoveSpecialCharactersWhenNotPropertyExpansion() throws Exception {
        String stringWithSpecialCharacters = "<xsd:attribute name=\"name\" type=\"xsd:string\" default=\"$ { #testxml#$ {testxpath} } ${\"/>";
        assertThat(PropertyExpansionRemover.removeExpansions(stringWithSpecialCharacters),
                is(stringWithSpecialCharacters));
    }

    @Test
    public void handlesNullValues() throws Exception {
        assertThat(PropertyExpansionRemover.removeExpansions(null), is(nullValue()));

    }
}
