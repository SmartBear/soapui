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

package com.eviware.soapui.security.ui;

import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.security.scan.GroovySecurityScan;
import com.eviware.soapui.support.components.GroovyEditorComponent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

public class GroovySecurityScanPanel extends SecurityScanConfigPanel {
    protected static final String SCRIPT_FIELD = "Script";

    private GroovySecurityScan groovyCheck;
    private GroovyEditorComponent groovyEditor;

    public GroovySecurityScanPanel(GroovySecurityScan securityCheck) {
        super(new BorderLayout());

        groovyCheck = securityCheck;

        add(buildSetupScriptPanel(securityCheck));
    }

    @Override
    public void save() {

    }

    private class ScriptGroovyEditorModel extends AbstractGroovyEditorModel {
        @Override
        public Action createRunAction() {
            return new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    // nothing happens!
                }
            };
        }

        public ScriptGroovyEditorModel(ModelItem modelItem) {
            super(new String[]{"parameters", "log", "context", "securityScan", "testStep"}, modelItem, "");
        }

        public String getScript() {
            return ((GroovySecurityScan) getModelItem()).getExecuteScript();
        }

        public void setScript(String text) {
            ((GroovySecurityScan) getModelItem()).setExecuteScript(text);
        }
    }

    protected GroovyEditorComponent buildSetupScriptPanel(SecurityScan securityCheck) {
        groovyEditor = new GroovyEditorComponent(new ScriptGroovyEditorModel(securityCheck.getModelItem()), null);
        groovyEditor.setPreferredSize(new Dimension(385, 150));
        return groovyEditor;
    }

}
