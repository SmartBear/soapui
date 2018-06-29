/*
 * SoapUI, Copyright (C) 2004-2017 SmartBear Software
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

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Anders Jaensson
 */
public class CaseInsensitiveFileFilterTest {

    private MockAsWar.CaseInsensitiveFileFilter caseInsensitiveFileFilter;

    @Before
    public void setup() {
        caseInsensitiveFileFilter = new MockAsWar.CaseInsensitiveFileFilter();
    }

    @Test
    public void doesNotAcceptNullFile() {
        boolean fileAccepted = caseInsensitiveFileFilter.accept(null);

        assertThat(fileAccepted, is(false));
    }

    @Test
    public void doesNotAcceptEmptyFile() {
        boolean fileAccepted = caseInsensitiveFileFilter.accept(new File(""));

        assertThat(fileAccepted, is(false));
    }

    @Test
    public void doesNotAcceptExcludedFileEvenIfCaseDoesNotMatch() {
        boolean fileAccepted = caseInsensitiveFileFilter.accept(new File("SomeServletThing"));

        assertThat(fileAccepted, is(false));
    }

    @Test
    public void doesNotAcceptExcludedFileIfExactMatch() {
        boolean fileAccepted = caseInsensitiveFileFilter.accept(new File("servlet"));

        assertThat(fileAccepted, is(false));
    }

    @Test
    public void acceptsFileIfOnlyPartOfFilenameMatches() {
        boolean fileAccepted = caseInsensitiveFileFilter.accept(new File("servlek"));

        assertThat(fileAccepted, is(true));
    }

    @Test
    public void acceptsFileThatIsNotExcluded() {
        boolean fileAccepted = caseInsensitiveFileFilter.accept(new File("FileToInclude"));

        assertThat(fileAccepted, is(true));
    }

}
