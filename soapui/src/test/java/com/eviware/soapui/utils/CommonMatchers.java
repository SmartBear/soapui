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

package com.eviware.soapui.utils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.mail.internet.ContentType;
import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

/**
 * Hamcrest matchers for common data types.
 */
public class CommonMatchers {

    public static Matcher<String> endsWith(final String suffix) {
        return new TypeSafeMatcher<String>() {
            @Override
            public boolean matchesSafely(String s) {
                return s.endsWith(suffix);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a string ending with " + suffix);
            }
        };
    }

    public static Matcher<String> startsWith(final String prefix) {
        return new TypeSafeMatcher<String>() {
            @Override
            public boolean matchesSafely(String s) {
                return s.startsWith(prefix);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a string starting with " + prefix);
            }
        };
    }

    public static Matcher<String> anEmptyString() {
        return new TypeSafeMatcher<String>() {
            @Override
            public boolean matchesSafely(String s) {
                return s.isEmpty();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an empty string");
            }
        };
    }

    public static Matcher<Object[]> anEmptyArray() {
        return new TypeSafeMatcher<Object[]>() {
            @Override
            public boolean matchesSafely(Object[] objects) {
                return objects != null && objects.length == 0;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an empty array");
            }
        };
    }

    public static Matcher<Collection> anEmptyCollection() {
        return new TypeSafeMatcher<Collection>() {
            @Override
            public boolean matchesSafely(Collection collection) {
                return collection.isEmpty();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an empty collection");
            }
        };
    }

    public static Matcher<Map> anEmptyMap() {
        return new TypeSafeMatcher<Map>() {
            @Override
            public boolean matchesSafely(Map collection) {
                return collection.isEmpty();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an empty map");
            }
        };
    }

    public static Matcher<Collection> aCollectionWithSize(final int size) {
        return new TypeSafeMatcher<Collection>() {
            @Override
            public boolean matchesSafely(Collection collection) {
                return collection != null && collection.size() == size;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a collection with " + size + " elements");
            }
        };
    }

    public static Matcher<Node> compliantWithSchema(final String schemaPath) {
        return new TypeSafeMatcher<Node>() {
            @Override
            public boolean matchesSafely(Node node) {
                URL schemaURL = CommonMatchers.class.getResource(schemaPath);
                if (schemaURL == null) {
                    throw new IllegalArgumentException("No schema found at " + schemaPath);
                }
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema;
                try {
                    schema = sf.newSchema(schemaURL);
                } catch (SAXException e) {
                    throw new IllegalArgumentException("The file at " + schemaURL + " does not contain a valid XML schema", e);
                }
                try {
                    schema.newValidator().validate(new DOMSource(node));
                    return true;
                } catch (SAXException e) {
                    return false;
                } catch (Exception e) {
                    throw new RuntimeException("Unexpected exception", e);
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an XML node compliant with the XML schema at " + schemaPath);
            }
        };
    }

    public static Matcher<ContentType> sameBaseContentType(final String contentType) {
        return new TypeSafeMatcher<ContentType>() {
            @Override
            public boolean matchesSafely(ContentType item) {
                return contentType.equals(item.getBaseType());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("the content type " + contentType);
            }
        };
    }

    public static Matcher<File> exists() {
        return new TypeSafeMatcher<File>() {
            @Override
            public boolean matchesSafely(File file) {
                return file.exists();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an existing file");
            }
        };
    }

    public static Matcher<Object> aNumber() {
        return new org.hamcrest.TypeSafeMatcher<Object>() {
            @Override
            protected boolean matchesSafely(Object o) {
                return o instanceof Number;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a number");
            }
        };
    }

}
