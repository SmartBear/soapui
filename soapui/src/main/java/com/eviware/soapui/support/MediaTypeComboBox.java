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

import com.eviware.soapui.impl.support.http.MediaType;

import javax.swing.JComboBox;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class MediaTypeComboBox extends JComboBox {
    public MediaTypeComboBox(final MediaType model) {
        super(getMediaTypes());

        setEditable(true);
        if (model.getMediaType() != null) {
            setSelectedItem(model.getMediaType());
        }

        addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                model.setMediaType(String.valueOf(getSelectedItem()));
            }
        });

    }

    public static Object[] getMediaTypes() {
        return new String[]{"application/json", "application/xml", "text/xml", "multipart/form-data", "multipart/mixed"};
    }

}
