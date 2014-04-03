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

package com.eviware.soapui.impl.wsdl.actions.support;

import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.HelpActionMarker;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Shows an online help page
 *
 * @author Ole.Matzura
 */

public class ShowOnlineSoapUIHelp extends AbstractSoapUIAction<ModelItem> implements HelpActionMarker {
    public static final String SOAPUI_ACTION_ID = "ShowOnlineSoapUIHelp";
    private String url;

    public ShowOnlineSoapUIHelp() {
        super("Online Help", "Show Online Help");
    }

    public ShowOnlineSoapUIHelp(String name, String url) {
        super(name, url);
        this.url = url;
    }

    public void perform(ModelItem target, Object param) {
        if (param == null && url == null) {
            UISupport.showErrorMessage("Missing help URL");
            return;
        }

        String url = param == null ? this.url : param.toString();
        if (!url.startsWith("http://")) {
            url = HelpUrls.HELP_URL_ROOT + url;
        }

        Tools.openURL(url);
    }
}
