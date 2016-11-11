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

package com.eviware.soapui.impl.wsdl.monitor;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author joel.jonsson
 */
public class ContentTypesTest {
    @Test
    public void emptyStringGivesWildcardContentType() {
        assertThat(ContentTypes.of("").toString(), is(""));
    }

    @Test
    public void separateTwoContentTypesWithCommaAndSpace() {
        assertThat(ContentTypes.of("text/plain,application/*").toString(), is("text/plain, application/*"));
    }

    @Test
    public void noContentTypeDoesMatchRequestWithContentType() {
        assertThat(ContentTypes.of("").matches("text/plain"), is(false));
    }

    @Test
    public void fullWildCardMatchesRequestWithContentType() {
        assertThat(ContentTypes.of("*/*").matches("text/plain"), is(true));
    }

    @Test
    public void fullWildCardMatchesRequestWithContentTypeAndParameters() {
        assertThat(ContentTypes.of("*/*").matches("text/plain; charset=utf-8"), is(true));
    }

    @Test
    public void wildCardPrimaryTypeMatchesRequestWithContentType() {
        assertThat(ContentTypes.of("*/plain").matches("text/plain"), is(true));
    }

    @Test
    public void wildCardPrimaryTypeDoesNotMatchRequestWithOtherSubtype() {
        assertThat(ContentTypes.of("*/xml").matches("text/plain"), is(false));
    }

    @Test
    public void wildCardSubTypeMatchesRequestWithContentType() {
        assertThat(ContentTypes.of("text/*").matches("text/plain"), is(true));
    }

    @Test
    public void wildCardSubTypeDoesNotMatchRequestWithOtherPrimaryType() {
        assertThat(ContentTypes.of("application/*").matches("text/plain"), is(false));
    }

    @Test
    public void equalContentTypesMatches() {
        assertThat(ContentTypes.of("text/plain").matches("text/plain"), is(true));
    }

    @Test
    public void equalContentTypesMatchesEvenWithParameters() {
        assertThat(ContentTypes.of("text/plain").matches("text/plain; charset=utf-8"), is(true));
    }

    @Test
    public void invalidContentTypeIsSilentlyIgnoredAndDoesNotMatch() {
        assertThat(ContentTypes.of("hejhopp").matches("hejhopp/tjoho"), is(false));
    }

    @Test
    public void invalidRetrievedContentTypeIsSilentlyIgnoredAndDoesNotMatch() {
        assertThat(ContentTypes.of("*/*").matches("hejhopp"), is(false));
    }

    @Test
    public void separateMatchingForSeveralContentTypes() {
        ContentTypes contentTypes = ContentTypes.of("text/plain, application/*");
        assertThat(contentTypes.matches("text/plain"), is(true));
        assertThat(contentTypes.matches("application/xml"), is(true));
        assertThat(contentTypes.matches("text/html"), is(false));
    }
}
