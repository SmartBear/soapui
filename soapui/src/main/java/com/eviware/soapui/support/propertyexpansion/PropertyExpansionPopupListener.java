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

package com.eviware.soapui.support.propertyexpansion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;

import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockService;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionImpl;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.GroovyEditorComponent;
import com.eviware.soapui.support.components.ShowPopupAction;
import com.eviware.soapui.support.propertyexpansion.scrollmenu.ScrollableMenu;
import com.eviware.soapui.support.xml.XmlUtils;

public class PropertyExpansionPopupListener implements PopupMenuListener {
    private final Container targetMenu;
    private final ModelItem modelItem;
    private final PropertyExpansionTarget target;

    public PropertyExpansionPopupListener(Container transferMenu, ModelItem modelItem, PropertyExpansionTarget target) {
        this.modelItem = modelItem;
        this.target = target;

        this.targetMenu = transferMenu;
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent arg0) {
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
        // create transfer menus
        targetMenu.removeAll();

        WsdlTestStep testStep;
        WsdlTestCase testCase = null;
        WsdlTestSuite testSuite = null;
        WsdlProject project = null;
        MockService mockService = null;
        MockResponse mockResponse = null;
        SecurityTest securityTest = null;

        if (modelItem instanceof WsdlTestStep) {
            testStep = (WsdlTestStep) modelItem;
            testCase = testStep.getTestCase();
            testSuite = testCase.getTestSuite();
            project = testSuite.getProject();
        } else if (modelItem instanceof WsdlTestCase) {
            testCase = (WsdlTestCase) modelItem;
            testSuite = testCase.getTestSuite();
            project = testSuite.getProject();
        } else if (modelItem instanceof WsdlTestSuite) {
            testSuite = (WsdlTestSuite) modelItem;
            project = testSuite.getProject();
        } else if (modelItem instanceof WsdlMockService) {
            project = ((WsdlMockService) modelItem).getProject();
        } else if (modelItem instanceof MockResponse) {
            mockResponse = (MockResponse) modelItem;
            mockService = (mockResponse).getMockOperation().getMockService();
            project = mockService.getProject();
        } else if (modelItem instanceof WsdlProject) {
            project = (WsdlProject) modelItem;
        } else if (modelItem instanceof AbstractHttpRequestInterface<?>) {
            project = ((AbstractHttpRequest<?>) modelItem).getOperation().getInterface().getProject();
        } else if (modelItem instanceof Operation) {
            project = (WsdlProject) ((Operation) modelItem).getInterface().getProject();
        } else if (modelItem instanceof SecurityTest) {
            securityTest = (SecurityTest) modelItem;
            testCase = securityTest.getTestCase();
            testSuite = testCase.getTestSuite();
            project = testSuite.getProject();
        }

        TestPropertyHolder globalProperties = PropertyExpansionUtils.getGlobalProperties();
        if (globalProperties.getProperties().size() > 0) {
            targetMenu.add(createPropertyMenu("Global", globalProperties));
        }

        if (project != null) {
            targetMenu.add(createPropertyMenu("Project: [" + project.getName() + "]", project));
        }

        if (testSuite != null) {
            targetMenu.add(createPropertyMenu("TestSuite: [" + testSuite.getName() + "]", testSuite));
        }

        if (mockService != null) {
            targetMenu.add(createPropertyMenu("MockService: [" + mockService.getName() + "]", mockService));
        }

        if (mockResponse != null) {
            targetMenu.add(createPropertyMenu("MockResponse: [" + mockResponse.getName() + "]", mockResponse));
        }

        if (testCase != null) {
            targetMenu.add(createPropertyMenu("TestCase: [" + testCase.getName() + "]", testCase));

            for (int c = 0; c < testCase.getTestStepCount(); c++) {
                testStep = testCase.getTestStepAt(c);
                if (testStep.getPropertyNames().length == 0) {
                    continue;
                }

                if (targetMenu.getComponentCount() == 3) {
                    targetMenu.add(new JSeparator());
                }

                targetMenu.add(createPropertyMenu("Step " + (c + 1) + ": [" + testStep.getName() + "]", testStep));
            }
        }

        if (securityTest != null) {
            targetMenu.add(createPropertyMenu("SecurityTest: [" + securityTest.getName() + "]", securityTest));
        }
    }

    private JMenu createPropertyMenu(String string, TestPropertyHolder holder) {
        ScrollableMenu menu = new ScrollableMenu(string);

        if (holder instanceof TestModelItem) {
            menu.setIcon(((TestModelItem) holder).getIcon());
        }

        String[] propertyNames = holder.getPropertyNames();

        for (String name : propertyNames) {
            menu.add(new TransferFromPropertyActionInvoker(holder, name));
        }

        if (holder instanceof MutableTestPropertyHolder) {
            menu.addHeader(new TransferFromPropertyActionInvoker((MutableTestPropertyHolder) holder));
        }

        return menu;
    }

    private class TransferFromPropertyActionInvoker extends AbstractAction {
        private final TestPropertyHolder sourceStep;
        private String sourceProperty;

        public TransferFromPropertyActionInvoker(TestPropertyHolder sourceStep, String sourceProperty) {
            super("Property [" + sourceProperty + "]");
            this.sourceStep = sourceStep;
            this.sourceProperty = sourceProperty;
        }

        public TransferFromPropertyActionInvoker(MutableTestPropertyHolder testStep) {
            super("Create new..");
            this.sourceStep = testStep;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (sourceProperty == null && sourceStep instanceof MutableTestPropertyHolder) {
                MutableTestPropertyHolder step = (MutableTestPropertyHolder) sourceStep;
                sourceProperty = target.getNameForCreation();

                sourceProperty = UISupport.prompt("Specify name of source property to create", "Create source property",
                        sourceProperty);
                while (sourceProperty != null && step.getProperty(sourceProperty) != null) {
                    sourceProperty = UISupport.prompt("Name is taken, specify unique name of source property to create",
                            "Create source property", sourceProperty);
                }

                if (sourceProperty == null) {
                    return;
                }

                ((MutableTestPropertyHolder) sourceStep).addProperty(sourceProperty);

                String newVal = UISupport.prompt("Specify the value of the new property '" + sourceProperty + "'",
                        "Set the value of the property", "");

                if (newVal != null) {
                    sourceStep.setPropertyValue(sourceProperty, newVal);
                }
            }

            String sourceXPath = "";

            String val = sourceStep.getPropertyValue(sourceProperty);

            try {
                if (StringUtils.isNullOrEmpty(val)) {
                    String defaultValue = sourceStep.getProperty(sourceProperty).getDefaultValue();
                    if (StringUtils.hasContent(defaultValue)) {
                        if (UISupport.confirm("Missing property value, use default value instead?", "Get Data")) {
                            val = defaultValue;
                        }
                    }
                }

                if (XmlUtils.seemsToBeXml(val)) {
                    // XmlObject.Factory.parse( val );
                    XmlUtils.createXmlObject(val);
                    sourceXPath = UISupport.selectXPath("Select XPath", "Select source xpath for property transfer", val,
                            null);
                }
            } catch (Throwable e) {
                // just ignore.. this wasn't xml..
            }

            if (StringUtils.hasContent(sourceXPath)) {
                sourceXPath = PropertyExpansionUtils.shortenXPathForPropertyExpansion(sourceXPath, val);
            }

            TestProperty property = sourceStep.getProperty(sourceProperty);
            PropertyExpansion pe = new PropertyExpansionImpl(property, sourceXPath);

            String userSelectedValue = target.getValueForCreation();
            target.insertPropertyExpansion(pe, null);

            if (!StringUtils.hasContent(sourceXPath) && StringUtils.hasContent(userSelectedValue)
                    && !property.isReadOnly()) {
                if (!userInputIsPropertyExpansion(userSelectedValue)) {
                    userSelectedValue = UISupport.prompt("Do you want to update the value of the property? (" + val + ")",
                            "Get Data", userSelectedValue);
                    if (userSelectedValue != null) {
                        property.setValue(userSelectedValue);
                    }
                }
            }
        }
    }

    private static final Pattern pattern = Pattern.compile("^\\$\\{(.*)\\}$");

    private static boolean userInputIsPropertyExpansion(String userSelectedValue) {
        if (userSelectedValue == null) {
            return false;
        }
        Matcher matcher = pattern.matcher(userSelectedValue);
        return matcher.matches();
    }

    public static void addMenu(JPopupMenu popup, String menuName, ModelItem item, PropertyExpansionTarget component) {
        ScrollableMenu menu = new ScrollableMenu(menuName);
        menu.setName(menuName);
        boolean contains = false;
        for (int i = 0; i < popup.getComponentCount(); i++) {
            if (menu.getName() != null && menu.getName().equals(popup.getComponent(i).getName())) {
                contains = true;
                break;
            }
        }
        if (!contains) {
            popup.add(menu);
            popup.addPopupMenuListener(new PropertyExpansionPopupListener(menu, item, component));
        }
    }

    public static void enable(JTextComponent textField, ModelItem modelItem, JPopupMenu popup) {
        JTextComponentPropertyExpansionTarget target = new JTextComponentPropertyExpansionTarget(textField, modelItem);
        DropTarget dropTarget = new DropTarget(textField, new PropertyExpansionDropTarget(target));
        dropTarget.setDefaultActions(DnDConstants.ACTION_COPY_OR_MOVE);

        textField.setComponentPopupMenu(popup);

        if (popup != null) {
            PropertyExpansionPopupListener.addMenu(popup, "Get Data..", target.getContextModelItem(), target);
        }
    }

    public static JPanel addPropertyExpansionPopup(JTextField textField, JPopupMenu popup, ModelItem modelItem) {
        PropertyExpansionPopupListener.enable(textField, modelItem, popup);

        JButton popupButton = new JButton();
        popupButton.setAction(new ShowPopupAction(textField, popupButton));
        popupButton.setBackground(Color.WHITE);
        popupButton.setForeground(Color.WHITE);
        popupButton.setBorder(null);
        popupButton.setOpaque(true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(textField, BorderLayout.CENTER);
        panel.add(popupButton, BorderLayout.EAST);
        panel.setBorder(textField.getBorder());
        textField.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        return panel;
    }

    public static void enable(RSyntaxTextArea textField, ModelItem modelItem) {
        RSyntaxTextAreaPropertyExpansionTarget target = new RSyntaxTextAreaPropertyExpansionTarget(textField, modelItem);
        DropTarget dropTarget = new DropTarget(textField, new PropertyExpansionDropTarget(target));
        dropTarget.setDefaultActions(DnDConstants.ACTION_COPY_OR_MOVE);

        JPopupMenu popup = textField.getPopupMenu();

        if (popup != null) {
            PropertyExpansionPopupListener.addMenu(popup, "Get Data..", target.getContextModelItem(), target);
        }
    }

    public static void enable(GroovyEditor groovyEditor, ModelItem modelItem) {
        GroovyEditorPropertyExpansionTarget target = new GroovyEditorPropertyExpansionTarget(groovyEditor, modelItem);
        DropTarget dropTarget = new DropTarget(groovyEditor.getEditArea(), new PropertyExpansionDropTarget(target));
        dropTarget.setDefaultActions(DnDConstants.ACTION_COPY_OR_MOVE);

        JPopupMenu popup = groovyEditor.getEditArea().getComponentPopupMenu();

        if (popup != null) {
            ScrollableMenu menu = new ScrollableMenu("Get Data..");
            popup.insert(menu, 0);
            popup.addPopupMenuListener(new PropertyExpansionPopupListener(menu, target.getContextModelItem(), target));
            popup.insert(new JSeparator(), 1);
        }
    }

    public static void enable(JTextComponent textField, ModelItem modelItem) {
        JPopupMenu popupMenu = textField.getComponentPopupMenu();
        if (popupMenu == null) {
            popupMenu = new JPopupMenu();
            textField.setComponentPopupMenu(popupMenu);
        }

        enable(textField, modelItem, popupMenu);
    }

    public static void enable(GroovyEditorComponent gec, ModelItem modelItem) {
        enable(gec.getEditor(), modelItem);
    }
}
