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

package com.eviware.soapui.model.testsuite;

import com.eviware.soapui.model.ModelItem;
import org.apache.xmlbeans.SchemaType;

import javax.xml.namespace.QName;

public interface TestProperty {
    public String getName();

    public String getDescription();

    public String getValue();

    public String getDefaultValue();

    public void setValue(String value);

    public boolean isReadOnly();

    public QName getType();

    /**
     * Gets the modelItem containing this property
     *
     * @return the modelItem containing this property
     */

    public ModelItem getModelItem();

    /**
     * defines if specific property belongs to request part
     */
    public boolean isRequestPart();

    public SchemaType getSchemaType();
}
