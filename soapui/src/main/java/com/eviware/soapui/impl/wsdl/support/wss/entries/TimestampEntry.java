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

package com.eviware.soapui.impl.wsdl.support.wss.entries;

import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.jgoodies.binding.PresentationModel;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecTimestamp;
import org.w3c.dom.Document;

import javax.swing.JComponent;

public class TimestampEntry extends WssEntryBase {
    public static final String TYPE = "Timestamp";

    private int timeToLive;
    private boolean strictTimestamp;

    public void init(WSSEntryConfig config, OutgoingWss container) {
        super.init(config, container, TYPE);
    }

    @Override
    protected JComponent buildUI() {
        SimpleBindingForm form = new SimpleBindingForm(new PresentationModel<TimestampEntry>(this));
        form.addSpace(5);
        form.appendTextField("timeToLive", "Time To Live", "Sets the TimeToLive value for the Timestamp Token");
        form.appendCheckBox("strictTimestamp", "Millisecond Precision", "Sets precision of timestamp to milliseconds");

        return form.getPanel();
    }

    @Override
    protected void load(XmlObjectConfigurationReader reader) {
        timeToLive = reader.readInt("timeToLive", 0);
        strictTimestamp = reader.readBoolean("strictTimestamp", true);
    }

    @Override
    protected void save(XmlObjectConfigurationBuilder builder) {
        builder.add("timeToLive", timeToLive);
        builder.add("strictTimestamp", strictTimestamp);
    }

    public void process(WSSecHeader secHeader, Document doc, PropertyExpansionContext context) {
        if (timeToLive <= 0) {
            return;
        }

        WSSecTimestamp timestamp = new WSSecTimestamp();
        timestamp.setTimeToLive(timeToLive);

        if (!strictTimestamp) {
            WSSConfig wsc = WSSConfig.getNewInstance();
            wsc.setPrecisionInMilliSeconds(false);
            wsc.setTimeStampStrict(false);
            timestamp.setWsConfig(wsc);
        }

        timestamp.build(doc, secHeader);
    }

    public String getTimeToLive() {
        return String.valueOf(timeToLive);
    }

    public boolean isStrictTimestamp() {
        return strictTimestamp;
    }

    public void setStrictTimestamp(boolean strictTimestamp) {
        this.strictTimestamp = strictTimestamp;
        saveConfig();
    }

    public void setTimeToLive(String timeToLive) {
        try {
            this.timeToLive = Integer.valueOf(timeToLive);
            saveConfig();
        } catch (Exception e) {
        }
    }
}
