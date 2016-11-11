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

package com.eviware.soapui.impl.wadl.inference;

import com.eviware.soapui.impl.wadl.inference.support.InferredSchemaImpl;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * XML Schema inferred from gathered XML data.
 *
 * @author Dain Nilsson
 */
public interface InferredSchema {

    /**
     * Method for reading out the inferred schema, in its current form.
     *
     * @return The inferred schema.
     */
    public SchemaTypeSystem getSchemaTypeSystem();

    /**
     * Expands the inferred schema to accept the input XML as valid.
     *
     * @param xml An XmlObject that is assumed to be valid.
     * @throws XmlException
     */
    public void processValidXml(XmlObject xml) throws XmlException;

    /**
     * Attempts to validate the given XML against the inferred schema. Any errors
     * detected will cause validation to halt and return false.
     *
     * @param xml
     * @return Returns true if the content validated successfully, false if not.
     */
    public boolean validate(XmlObject xml);

    /**
     * Attempts to validate the given XML against the inferred schema. Any errors
     * detected need to be resolved to either expand the schema, or the input
     * will cause an XmlException to be thrown.
     *
     * @param xml
     * @param handler
     * @throws XmlException for validation error.
     */
    public void learningValidate(XmlObject xml, ConflictHandler handler) throws XmlException;

    /**
     * Writes the XML represented by this InferredSchema.
     *
     * @param os
     * @throws IOException
     */
    public void save(OutputStream os) throws IOException;

    /**
     * Returns a string representation of the XML Schema for a particular
     * namespace, if available.
     *
     * @param namespace
     * @return A String representation of the XML Schema describing the
     *         namespace.
     */
    public String getXsdForNamespace(String namespace);

    /**
     * Returns a list of inferred namespaces.
     *
     * @return A Set containing all inferred namespaces.
     */
    public String[] getNamespaces();

    /**
     * Static factory class for creating new instances.
     *
     * @author Dain Nilsson
     */
    static class Factory {

        /**
         * Creates a new empty schema.
         *
         * @return A new, blank InferredSchema.
         */
        public static InferredSchema newInstance() {
            return new InferredSchemaImpl();
        }

        /**
         * Decodes and parses the given InputStream as a serialized
         * InferredSchema.
         *
         * @param is
         * @return An InferredSchema containing previously saved data.
         * @throws XmlException
         * @throws IOException
         */
        public static InferredSchema parse(InputStream is) throws XmlException, IOException {
            return new InferredSchemaImpl(is);
        }
    }

    public void deleteNamespace(String ns);

    public SchemaTypeSystem getSchemaTypeSystem(SchemaTypeSystem schemaTypeSystem);

}
