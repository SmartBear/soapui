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

package com.eviware.soapui.support.editor.inspectors.wss;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlLocation;

import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Vector;

public abstract class AbstractWssInspector extends AbstractXmlInspector {
    private JPanel mainPanel;
    private JList resultList;

    protected AbstractWssInspector() {
        super("WSS", "Displays WS-Security information for this response", true, WssInspectorFactory.INSPECTOR_ID);
    }

    @Override
    public void release() {
        super.release();
    }

    public void locationChanged(XmlLocation location) {
    }

    public JComponent getComponent() {
        if (mainPanel == null) {
            mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(buildContent(), BorderLayout.CENTER);

            UISupport.addTitledBorder(mainPanel, "WS-Security processing results");

            update();
        }

        return mainPanel;
    }

    private Component buildContent() {
        resultList = new JList(new ResultVectorListModel(getWssResults()));
        return new JScrollPane(resultList);
    }

    public abstract Vector<?> getWssResults();

    public void update() {
        resultList.setModel(new ResultVectorListModel(getWssResults()));
        int size = resultList.getModel().getSize();
        setTitle("WSS (" + size + ")");
        setEnabled(size > 0);
    }

    private static class ResultVectorListModel extends AbstractListModel {
        private final Vector<?> result;

        public ResultVectorListModel(Vector<?> result) {
            this.result = result;
        }

        public Object getElementAt(int index) {
            return result == null ? null : result.get(index);
        }

        public int getSize() {
            return result == null ? 0 : result.size();
        }
    }

    @Override
    public boolean isEnabledFor(EditorView<XmlDocument> view) {
        return true;
    }
}
