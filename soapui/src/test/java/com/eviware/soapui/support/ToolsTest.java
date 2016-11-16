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

package com.eviware.soapui.support;

import junit.framework.ComparisonFailure;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class ToolsTest {
    @Test
    public void tokenizesArgs() throws Exception {
        assertNull(Tools.tokenizeArgs(""));

        String[] args = Tools.tokenizeArgs("test ett");
        assertEquals(args.length, 2);

        args = Tools.tokenizeArgs("\"test ett\"");
        assertEquals(args.length, 1);
        assertEquals(args[0], "test ett");

        args = Tools.tokenizeArgs("\"test\\\" ett\" ");
        assertEquals(args.length, 1);
        assertEquals(args[0], "test\" ett");
    }

    @Test
    public void joinsUrlsCorrectly() throws Exception {
        assertEquals("http://test:8080/my/root/test.xsd",
                Tools.joinRelativeUrl("http://test:8080/my/root/test.wsdl", "test.xsd"));
        assertEquals("http://test:8080/my/root/bu/test.xsd",
                Tools.joinRelativeUrl("http://test:8080/my/root/test.wsdl", "bu/test.xsd"));
        assertEquals("http://test:8080/my/test.xsd",
                Tools.joinRelativeUrl("http://test:8080/my/root/test.wsdl", "../test.xsd"));
        assertEquals("http://test:8080/my/root/test.xsd",
                Tools.joinRelativeUrl("http://test:8080/my/root/test.wsdl", "./test.xsd"));
        assertEquals("http://test:8080/bil/test.xsd",
                Tools.joinRelativeUrl("http://test:8080/my/root/test.wsdl", "../../bil/test.xsd"));
        assertEquals("http://test:8080/bil/test.xsd",
                Tools.joinRelativeUrl("http://test:8080/my/root/test.wsdl", "././../../bil/test/.././test.xsd"));
        assertEquals("file:c:" + File.separator + "bil" + File.separator + "xsd" + File.separator + "test.xsd", Tools.joinRelativeUrl("file:c:\\bil\\test.wsdl", "./xsd/test.xsd"));
        assertEquals("file:c:" + File.separator + "bil" + File.separator + "xsd" + File.separator + "test.xsd",
                Tools.joinRelativeUrl("file:c:\\bil\\test\\test\\test.wsdl", "..\\..\\xsd\\test.xsd"));
    }

    @Test
    public void onlyWildcardMatches() {
        Tools.assertSimilar("*", "abc", '*');
    }
    @Test
    public void exactMatches() {
        Tools.assertSimilar("abc", "abc", '*');
    }

    @Test
    public void wildCardInBeginningMatches() {
        Tools.assertSimilar("*abc", "abc", '*');
        Tools.assertSimilar("*bc", "abc", '*');
    }

    @Test
    public void wildCardInMiddleMatches() {
        Tools.assertSimilar("a*c", "abc", '*');
    }

    @Test
    public void wildCardInEndMatches() {
        Tools.assertSimilar("abc*", "abc", '*');
        Tools.assertSimilar("ab*", "abc", '*');
    }

    @Test
    public void wildCardInBeginningAndEndMatches() {
        Tools.assertSimilar("*abc*", "abc", '*');
        Tools.assertSimilar("*bc*", "abc", '*');
        Tools.assertSimilar("*ab*", "abc", '*');
        Tools.assertSimilar("*b*", "abc", '*');
    }

    @Test(expected = ComparisonFailure.class)
    public void mixingUpOrderBetweenWildcardsDoesNotMatch() {
        Tools.assertSimilar("The*in*mainly*rain*Spain*stays*the*plain!", "The rain in Spain stays mainly in the plain!", '*');
    }

    @Test(expected = ComparisonFailure.class)
    public void differentCasingDoesNotMatch() {
        Tools.assertSimilar("heLLo", "HEllO", '*');
    }

    @Test
    public void wildcardMatchesMultipleLine() {
        assertThat(Tools.isSimilar("first*rd line", "first line\nsecond line\third line", '*'), is(true));
    }
}
