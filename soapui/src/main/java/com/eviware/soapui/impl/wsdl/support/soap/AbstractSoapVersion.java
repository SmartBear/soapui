/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.support.soap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Common behaviour for all SOAP Versions
 *
 * @author ole.matzura
 */

public abstract class AbstractSoapVersion implements SoapVersion {
    private final static Logger log = LogManager.getLogger(AbstractSoapVersion.class);

    @SuppressWarnings("unchecked")
    public void validateSoapEnvelope(String soapMessage, List<XmlError> errors) {
        List<XmlError> errorList = new ArrayList<XmlError>();

        try {
            XmlOptions xmlOptions = new XmlOptions();
            xmlOptions.setLoadLineNumbers();
            xmlOptions.setValidateTreatLaxAsSkip();
            xmlOptions.setLoadLineNumbers(XmlOptions.LOAD_LINE_NUMBERS_END_ELEMENT);
            XmlObject xmlObject = getSoapEnvelopeSchemaLoader().parse(soapMessage, getEnvelopeType(), xmlOptions);
            xmlOptions.setErrorListener(errorList);
            xmlObject.validate(xmlOptions);
        } catch (XmlException e) {
            if (e.getErrors() != null) {
                errorList.addAll(e.getErrors());
            }

            errors.add(XmlError.forMessage(e.getMessage()));
        } catch (Exception e) {
            errors.add(XmlError.forMessage(e.getMessage()));
        } finally {
            for (XmlError error : errorList) {
                if (error instanceof XmlValidationError && shouldIgnore((XmlValidationError) error)) {
                    log.warn("Ignoring validation error: " + error.toString());
                    continue;
                }

                errors.add(error);
            }
        }
    }

    protected abstract SchemaTypeLoader getSoapEnvelopeSchemaLoader();

    public boolean shouldIgnore(XmlValidationError error) {
        QName offendingQName = error.getOffendingQName();
        if (offendingQName != null) {
            if (offendingQName.equals(new QName(getEnvelopeNamespace(), "encodingStyle"))) {
                return true;
            } else if (offendingQName.equals(new QName(getEnvelopeNamespace(), "mustUnderstand"))) {
                return true;
            }
        }

        return false;
    }

    public abstract SchemaType getFaultType();

    public abstract SchemaType getEnvelopeType();
}
