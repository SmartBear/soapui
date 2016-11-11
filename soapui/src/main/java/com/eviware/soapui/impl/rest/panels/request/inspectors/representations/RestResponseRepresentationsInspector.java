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

package com.eviware.soapui.impl.rest.panels.request.inspectors.representations;

import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlCursor;

import javax.swing.JCheckBox;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RestResponseRepresentationsInspector extends AbstractRestRepresentationsInspector implements
        SubmitListener {
    private JCheckBox enableRecordingCheckBox;
    public static final String RECORD_RESPONSE_REPRESENTATIONS = "RecordResponseRepresentations";
    private RestRequestInterface request;

    protected RestResponseRepresentationsInspector(RestRequestInterface request) {
        super(request.getRestMethod(), "Representations", "Response Representations", new RestRepresentation.Type[]{
                RestRepresentation.Type.RESPONSE, RestRepresentation.Type.FAULT});

        request.addSubmitListener(this);
        this.request = request;
    }

    @Override
    protected void addToToolbar(JXToolBar toolbar) {
        enableRecordingCheckBox = new JCheckBox("Auto-Create");
        enableRecordingCheckBox.setToolTipText("Automatically create Representations from received Responses");
        enableRecordingCheckBox.setOpaque(false);
        UISupport.setFixedSize(enableRecordingCheckBox, 150, 20);
        toolbar.addFixed(enableRecordingCheckBox);
        XmlBeansSettingsImpl settings = getMethod().getSettings();
        if (settings.isSet(RECORD_RESPONSE_REPRESENTATIONS)) {
            enableRecordingCheckBox.setSelected(settings.getBoolean(RECORD_RESPONSE_REPRESENTATIONS));
        } else {
            enableRecordingCheckBox.setSelected(getMethod().getResource() == null
                    || getMethod().getResource().getService().isGenerated());
        }

        enableRecordingCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                getMethod().getSettings()
                        .setBoolean(RECORD_RESPONSE_REPRESENTATIONS, enableRecordingCheckBox.isSelected());
            }
        });
    }

    @Override
    public boolean beforeSubmit(Submit submit, SubmitContext context) {
        return true;
    }

    public void afterSubmit(Submit submit, SubmitContext context) {
        HttpResponse response = (HttpResponse) submit.getResponse();
        if (response != null && enableRecordingCheckBox.isSelected()) {
            if (HttpUtils.isErrorStatus(response.getStatusCode())) {
                extractRepresentation(response, RestRepresentation.Type.FAULT);
            } else {
                extractRepresentation(response, RestRepresentation.Type.RESPONSE);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void extractRepresentation(HttpResponse response, RestRepresentation.Type type) {
        RestRepresentation[] representations = getMethod().getRepresentations(type, null);
        int c = 0;
        for (; c < representations.length; c++) {
            if (representations[c].getMediaType() != null
                    && representations[c].getMediaType().equals(response.getContentType())) {
                @SuppressWarnings("rawtypes")
                List status = representations[c].getStatus();
                if (status == null || !status.contains(response.getStatusCode())) {
                    status = status == null ? new ArrayList<Integer>() : new ArrayList<Integer>(status);
                    status.add(response.getStatusCode());
                    representations[c].setStatus(status);
                }
                break;
            }
        }

        if (c == representations.length) {
            RestRepresentation representation = getMethod().addNewRepresentation(type);
            representation.setMediaType(response.getContentType());
            representation.setStatus(Arrays.asList(response.getStatusCode()));

            String xmlContent = response.getContentAsXml();

            if (xmlContent != null && !xmlContent.equals("<xml/>")) {
                // if(response.getContentType().equals("text/xml") ||
                // response.getContentType().equals("application/xml")) {
                try {
                    // XmlCursor cursor = XmlObject.Factory.parse( xmlContent
                    // ).newCursor();
                    XmlCursor cursor = XmlUtils.createXmlObject(xmlContent).newCursor();
                    cursor.toFirstChild();
                    representation.setElement(cursor.getName());
                } catch (Exception e) {

                }
            }
        }
    }

    @Override
    public void release() {
        super.release();
        request.removeSubmitListener(this);
    }
}
