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

package com.eviware.soapui.actions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;
import com.eviware.soapui.support.xml.XmlUtils;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;

/**
 * Action for starting XQuery/XPath tester - not used for now..
 *
 * @author Ole.Matzura
 */

public class XQueryXPathTesterAction extends AbstractAction {
    private JDialog dialog;
    private JSplitPane mainSplit;
    private RSyntaxTextArea resultArea;
    private JSplitPane querySplit;
    private RSyntaxTextArea inputArea;
    private JTextArea xqueryArea;
    private JTextArea xpathArea;
    private JTabbedPane queryTabs;
    private JLabel statusLabel;

    public XQueryXPathTesterAction() {
        super("XQuery/XPath Tester");
    }

    public void actionPerformed(ActionEvent e) {
        if (dialog == null) {
            buildDialog();
        }

        dialog.setVisible(true);
    }

    private void buildDialog() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        mainSplit = UISupport.createHorizontalSplit(createQueryPanel(), createResultPanel());
        mainSplit.setResizeWeight(0.4);
        panel.add(mainSplit, BorderLayout.CENTER);
        panel.add(createStatusBar(), BorderLayout.SOUTH);

        dialog = new JDialog(UISupport.getMainFrame(), "XQuery / XPath Tester", false);
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.setPreferredSize(new Dimension(600, 400));
        dialog.pack();

        mainSplit.setDividerLocation(0.5);
        querySplit.setDividerLocation(0.3);
    }

    private JPanel createToolbar() {
        ButtonBarBuilder builder = new ButtonBarBuilder();

        JButton runButton = UISupport.createToolbarButton(new RunAction());
        builder.addFixed(runButton);
        builder.addRelatedGap();
        JButton declareNsButton = UISupport.createToolbarButton(new DeclareNSAction());
        builder.addFixed(declareNsButton);
        builder.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        return builder.getPanel();
    }

    private JComponent createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        statusLabel = new JLabel();
        panel.add(statusLabel, BorderLayout.WEST);
        return panel;
    }

    private JPanel createQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        querySplit = UISupport.createVerticalSplit(buildQueryTabs(), buildInputArea());
        querySplit.setBorder(null);
        querySplit.setResizeWeight(0.2);
        panel.add(querySplit, BorderLayout.CENTER);

        return panel;
    }

    private JComponent buildQueryTabs() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createToolbar(), BorderLayout.NORTH);

        queryTabs = new JTabbedPane();
        queryTabs.addTab("XQuery query", buildXQueryArea());
        queryTabs.addTab("XPath query", buildXPathArea());
        queryTabs.setTabPlacement(JTabbedPane.BOTTOM);

        panel.setBackground(Color.LIGHT_GRAY);
        panel.add(queryTabs, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildXQueryArea() {
        xqueryArea = new JTextArea();
        return new JScrollPane(xqueryArea);
    }

    private JComponent buildXPathArea() {
        xpathArea = new JTextArea();
        return new JScrollPane(xpathArea);
    }

    private JComponent buildInputArea() {
        inputArea = SyntaxEditorUtil.createDefaultXmlSyntaxTextArea();
        return inputArea;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        resultArea = SyntaxEditorUtil.createDefaultXmlSyntaxTextArea();
        panel.add(resultArea, BorderLayout.CENTER);

        return panel;
    }

    private class RunAction extends AbstractAction {
        public RunAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/submit_request.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Execute current query");
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("alt ENTER"));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                // XmlObject xmlObject = XmlObject.Factory.parse(
                // inputArea.getText() );
                XmlObject xmlObject = XmlUtils.createXmlObject(inputArea.getText());
                XmlObject[] objects;

                // xquery?
                if (queryTabs.getSelectedIndex() == 0) {
                    objects = xmlObject.execQuery(xqueryArea.getText());
                } else {
                    objects = xmlObject.selectPath(xpathArea.getText());
                }

                StringBuffer result = new StringBuffer();
                XmlOptions options = new XmlOptions();
                options.setSaveOuter();

                for (int c = 0; c < objects.length; c++) {

                    result.append(objects[c].xmlText(options));
                    result.append("\n");
                }

                resultArea.setText(result.toString());
                statusLabel.setText("Expression returned " + objects.length + " hits");
            } catch (Throwable e1) {
                if (e1 instanceof RuntimeException) {
                    e1 = ((RuntimeException) e1).getCause();
                    if (e1 instanceof InvocationTargetException) {
                        e1 = ((InvocationTargetException) e1).getTargetException();
                    }
                }

                statusLabel.setText(e1.getMessage());
            }
        }
    }

    private class DeclareNSAction extends AbstractAction {
        public DeclareNSAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/declareNs.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Declares namespaces in current input in xpath expression");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                String namespaceDeclarations = XmlUtils.declareXPathNamespaces(inputArea.getText());
                xpathArea.setText(namespaceDeclarations + xpathArea.getText());
                xqueryArea.setText(namespaceDeclarations + xqueryArea.getText());
            } catch (XmlException e1) {
                SoapUI.logError(e1);
            }
        }
    }

}
