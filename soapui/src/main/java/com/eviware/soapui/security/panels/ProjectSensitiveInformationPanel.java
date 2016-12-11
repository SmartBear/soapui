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

package com.eviware.soapui.security.panels;

import com.eviware.soapui.config.ProjectConfig;
import com.eviware.soapui.config.SensitiveInformationConfig;
import com.eviware.soapui.model.security.SensitiveInformationTableModel;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.security.SensitiveInformationPropertyHolder;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.JTableFactory;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import org.apache.xmlbeans.XmlObject;
import org.jdesktop.swingx.JXTable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ProjectSensitiveInformationPanel {

    private JPanel mainpanel;
    private SensitiveInformationConfig config;
    private List<String> projectSpecificExposureList;
    public static final String PROJECT_SPECIFIC_EXPOSURE_LIST = "ProjectSpecificExposureList";
    private SensitiveInformationTableModel sensitiveInformationTableModel;
    private JXTable tokenTable;

    public ProjectSensitiveInformationPanel(ProjectConfig projectConfig) {
        config = projectConfig.getSensitiveInformation();
        if (config == null) {
            config = SensitiveInformationConfig.Factory.newInstance();
            projectConfig.addNewSensitiveInformation();
            projectConfig.setSensitiveInformation(config);
        }
        init();
    }

    private void init() {
        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(config);
        projectSpecificExposureList = StringUtils.toStringList(reader.readStrings(PROJECT_SPECIFIC_EXPOSURE_LIST));
        extractTokenTable();
    }

    private void extractTokenTable() {
        SensitiveInformationPropertyHolder siph = new SensitiveInformationPropertyHolder();
        for (String str : projectSpecificExposureList) {
            String[] tokens = str.split("###");
            if (tokens.length == 2) {
                siph.setPropertyValue(tokens[0], tokens[1]);
            } else {
                siph.setPropertyValue(tokens[0], "");
            }
        }
        sensitiveInformationTableModel = new SensitiveInformationTableModel(siph, "Sensitive Information Token");
    }

    public Component getMainPanel() {
        if (mainpanel == null) {
            mainpanel = new JPanel(new BorderLayout());

            JXToolBar toolbar = UISupport.createToolbar();

            toolbar.add(UISupport.createToolbarButton(new AddTokenAction()));
            toolbar.add(UISupport.createToolbarButton(new RemoveTokenAction()));

            tokenTable = JTableFactory.getInstance().makeJXTable(sensitiveInformationTableModel);

//            mainpanel.add(new JLabel("Sensitive Information Tokens:"), BorderLayout.WEST);
            mainpanel.add(toolbar, BorderLayout.NORTH);
            mainpanel.add(new JScrollPane(tokenTable), BorderLayout.CENTER);
        }

        return mainpanel;
    }

    public void save() {
        projectSpecificExposureList = createListFromTable();
        setConfiguration(createConfiguration());
    }

    private List<String> createListFromTable() {
        List<String> temp = new ArrayList<String>();
        for (TestProperty tp : sensitiveInformationTableModel.getHolder().getPropertyList()) {
            String tokenPlusDescription = tp.getName() + "###" + tp.getValue();
            temp.add(tokenPlusDescription);
        }
        return temp;
    }

    protected XmlObject createConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        builder.add(PROJECT_SPECIFIC_EXPOSURE_LIST,
                projectSpecificExposureList.toArray(new String[projectSpecificExposureList.size()]));
        return builder.finish();
    }

    public void setConfiguration(XmlObject configuration) {
        config.set(configuration);
    }

    class AddTokenAction extends AbstractAction {

        public AddTokenAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/add.png"));
            putValue(Action.SHORT_DESCRIPTION, "Adds a token to assertion");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            String newToken = "";
            newToken = UISupport.prompt("Enter token", "New Token", newToken);
            String newValue = "";
            newValue = UISupport.prompt("Enter description", "New Description", newValue);

            sensitiveInformationTableModel.addToken(newToken, newValue);
            save();
        }

    }

    class RemoveTokenAction extends AbstractAction {

        public RemoveTokenAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/delete.png"));
            putValue(Action.SHORT_DESCRIPTION, "Removes token from assertion");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            sensitiveInformationTableModel.removeRows(tokenTable.getSelectedRows());
            save();
        }
    }

    public void release() {
        //
    }
}
