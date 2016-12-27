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

package com.eviware.soapui.plugins;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class VersionTest {

    public static final Version BASE_VERSION = Version.fromString("1.7");

    @Test
    public void correctEquals() throws Exception {
        assertThat(BASE_VERSION, is(equalTo(Version.fromString("1.7"))));
        assertThat(BASE_VERSION, is(not(equalTo(Version.fromString("1.7.1")))));
        assertThat(BASE_VERSION, is(not(equalTo(null))));
        assertThat(Version.fromString("1.7.2"), is(equalTo(Version.fromString("1.7.2"))));
        assertThat(Version.fromString("1.7.2"), is(not(equalTo(Version.fromString("1.7.3")))));
    }

    @Test
    public void correctHashCode() throws Exception {
        assertThat(BASE_VERSION.hashCode(), is(equalTo(Version.fromString("1.7").hashCode())));
        assertThat(BASE_VERSION.hashCode(), is(not(equalTo(Version.fromString("1.7.1").hashCode()))));
    }

    @Test
    public void compareTo() throws Exception {
        assertThat(Version.fromString("1.1"), is(greaterThan(Version.fromString("1.0"))));
        assertThat(Version.fromString("1.1.1"), is(greaterThan(Version.fromString("1.1.0"))));
        assertThat(Version.fromString("1.1.1"), is(greaterThan(Version.fromString("1.1"))));
    }

    private <T> Matcher<Comparable<T>> greaterThan(final T version) {
        return new TypeSafeMatcher<Comparable<T>>() {
            @Override
            protected boolean matchesSafely(Comparable<T> other) {
                return other.compareTo(version) > 0;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an object that is considered 'greater' than " + version);
            }
        };
    }
}
