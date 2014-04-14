/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.impl.support.panels;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.support.handlers.JsonXmlSerializer;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.submit.transports.http.DocumentContent;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;
import com.eviware.soapui.support.xml.XmlUtils;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.groovy.JsonSlurper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.StringReader;

import static com.eviware.soapui.impl.rest.support.handlers.JsonMediaTypeHandler.seemsToBeJsonContentType;

public abstract class AbstractHttpXmlRequestDesktopPanel<T extends ModelItem, T2 extends HttpRequestInterface<?>>
        extends AbstractHttpRequestDesktopPanel<T, T2> {

    public AbstractHttpXmlRequestDesktopPanel(T modelItem, T2 requestItem) {
        super(modelItem, requestItem);
    }

    @Override
    protected ModelItemXmlEditor<?, ?> buildRequestEditor() {
        return new HttpRequestMessageEditor(getRequest());
    }

    @Override
    protected ModelItemXmlEditor<?, ?> buildResponseEditor() {
        return new HttpResponseMessageEditor(getRequest());
    }

    public class HttpRequestMessageEditor extends
            AbstractHttpRequestDesktopPanel.AbstractHttpRequestMessageEditor {
        public HttpRequestMessageEditor(HttpRequestInterface<?> modelItem) {
            super(new HttpRequestDocument(modelItem));
        }
    }

    public class HttpResponseMessageEditor extends
            AbstractHttpRequestDesktopPanel.AbstractHttpResponseMessageEditor {
        public HttpResponseMessageEditor(HttpRequestInterface<?> modelItem) {
            super(new HttpResponseDocument(modelItem));
        }
    }

    public static class HttpRequestDocument extends AbstractXmlDocument implements PropertyChangeListener {
        private final HttpRequestInterface<?> request;
        private boolean updating;

        public HttpRequestDocument(HttpRequestInterface<?> request) {
            this.request = request;

            request.addPropertyChangeListener(this);
        }

        public HttpRequestInterface<?> getRequest() {
            return request;
        }

        public String getXml() {
            return getRequest().getRequestContent();
        }

        @Override
        public void release() {
            super.release();
            request.removePropertyChangeListener(this);
        }

        public void setXml(String xml) {
            if (!updating) {
                updating = true;
                try {
                    if (seemsToBeJsonContentType(getRequest().getMediaType()) && XmlUtils.seemsToBeXml(xml)) {
                        JSON json = new JsonXmlSerializer().read(xml);
                        processNullsAndEmptyValuesIn(json);
                        request.setRequestContent(json.toString(3, 0));
                    } else {
                        request.setRequestContent(xml);
                    }
                } finally {
                    updating = false;
                }
            }
        }

        private void processNullsAndEmptyValuesIn(JSON json) {
            String requestContent = request.getRequestContent();
            if (!StringUtils.hasContent(requestContent)) {
                return;
            }
            try {
                JSON oldJson = new JsonSlurper().parse(new StringReader(requestContent));
                if (!(json instanceof JSONObject) || !(oldJson instanceof JSONObject)) {
                    return;
                }
                overwriteNullValues((JSONObject) json, (JSONObject) oldJson);
            } catch (Exception e) {
                SoapUI.logError(e, "Unexpected error while parsing JSON");
            }
        }

        private void overwriteNullValues(JSONObject json, JSONObject oldJson) {
            for (Object key : json.keySet()) {
                Object value = json.get(key);
                Object oldValue = oldJson.get(key);
                if (isNullValue(value) && isEmptyJson(oldValue)) {
                    json.put(key, oldJson.get(key));
                } else if (isEmptyJson(value) && oldValue instanceof String) {
                    json.put(key, "");
                }
                //TODO: do this recursively but make sure that cyclic dependencies are handled
                /*else if ( value instanceof JSONObject && oldJson.get(key) instanceof JSONObject)
                {
					overwriteNullValues( (JSONObject) value, (JSONObject) oldJson.get(key) );
				}*/
            }
        }

        private boolean isEmptyJson(Object oldValue) {
            return oldValue != null && oldValue instanceof JSON && ((JSON) oldValue).isEmpty();
        }

        private boolean isNullValue(Object value) {
            return value == null || value.toString().equals("null");
        }


        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(RestRequestInterface.REQUEST_PROPERTY) && !updating) {
                updating = true;
                fireXmlChanged((String) evt.getOldValue(), (String) evt.getNewValue());
                updating = false;
            }
        }
    }

    public static class HttpResponseDocument extends AbstractXmlDocument implements PropertyChangeListener {
        private final HttpRequestInterface<?> modelItem;

        public HttpResponseDocument(HttpRequestInterface<?> modelItem) {
            this.modelItem = modelItem;

            modelItem.addPropertyChangeListener(RestRequestInterface.RESPONSE_PROPERTY, this);
        }

        public HttpRequestInterface<?> getRequest() {
            return modelItem;
        }

        public String getXml() {
            return modelItem.getResponseContentAsXml();
        }

        public void setXml(String xml) {
            HttpResponse response = getRequest().getResponse();
            if (response != null) {
                response.setResponseContent(xml);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            HttpResponse oldResponse = (HttpResponse) evt.getOldValue();
            HttpResponse response = (HttpResponse) evt.getNewValue();
            fireContentChanged(extractContentFrom(oldResponse), extractContentFrom(response));
            fireXmlChanged(oldResponse == null ? null : oldResponse.getContentAsString(), getXml());
        }

        private DocumentContent extractContentFrom(HttpResponse oldResponse) {
            if (oldResponse == null) {
                return null;
            } else {
                return new DocumentContent(oldResponse.getContentType(), oldResponse.getContentAsString());
            }
        }

        public void release() {
            super.release();
            modelItem.removePropertyChangeListener(RestRequestInterface.RESPONSE_PROPERTY, this);
        }
    }


}
