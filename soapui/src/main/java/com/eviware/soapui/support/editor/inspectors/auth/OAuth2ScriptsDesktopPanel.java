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

package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * A simple desktop panel wrapping the OAuth2ScriptsEditor component.
 *
 * @see OAuth2ScriptsEditor
 */
public class OAuth2ScriptsDesktopPanel extends DefaultDesktopPanel {
    public OAuth2ScriptsDesktopPanel(OAuth2Profile profile) {
        super("Automation scripts for OAuth 2 profile",
                "A panel used to edit JavaScript fragments that automate user interactions in an OAuth2 flow",
                new JPanel(new BorderLayout()));
        JPanel contentPane = (JPanel) getComponent();
        OAuth2ScriptsEditor editor = new OAuth2ScriptsEditor(profile);
        editor.setPreferredSize(new Dimension(900, 700));
        contentPane.add(editor, BorderLayout.CENTER);
    }
}
