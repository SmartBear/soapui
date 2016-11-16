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

package com.eviware.soapui.impl.wadl.inference.schema.content;

import com.eviware.soapui.impl.wadl.inference.schema.Content;
import com.eviware.soapui.impl.wadl.inference.schema.Context;
import com.eviware.soapui.impl.wadl.inference.schema.Schema;
import com.eviware.soapui.impl.wadl.inference.support.TypeInferrer;
import com.eviware.soapui.inferredSchema.EmptyContentConfig;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;

/**
 * EmptyContent may not have any content, be it simpe or complex.
 *
 * @author Dain Nilsson
 */
public class EmptyContent implements Content {
    private Schema schema;
    private boolean completed = false;

    public EmptyContent(Schema schema, boolean completed) {
        this.schema = schema;
        this.completed = completed;
    }

    public EmptyContent(EmptyContentConfig xml, Schema schema) {
        this.schema = schema;
        completed = xml.getCompleted();
    }

    public EmptyContentConfig save() {
        EmptyContentConfig xml = EmptyContentConfig.Factory.newInstance();
        xml.setCompleted(completed);
        return xml;
    }

    public String toString(String attrs) {
        return attrs;
    }

    public Content validate(Context context) throws XmlException {
        XmlCursor cursor = context.getCursor();
        cursor.push();
        if (cursor.toParent() && cursor.toFirstChild()) {
            // Element has children
            cursor.pop();
            return new SequenceContent(schema, completed);
        } else if (cursor.pop() && !cursor.isEnd()) {
            // Element has simple content
            if (completed) {
                return new SimpleContent(schema, TypeInferrer.getBlankType());
            } else {
                return new SimpleContent(schema, cursor.getTextValue());
            }
        }
        completed = true;
        return this;
    }

}
