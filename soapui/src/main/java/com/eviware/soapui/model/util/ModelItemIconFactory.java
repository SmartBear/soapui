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

package com.eviware.soapui.model.util;

import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.UISupport;

import javax.swing.ImageIcon;
import java.util.HashMap;
import java.util.Map;

public class ModelItemIconFactory {
    private static Map<Class<? extends ModelItem>, String> modelItemIcons = new HashMap<Class<? extends ModelItem>, String>();

    static {
        // the "class" keys here are only used for lookup - but must be implementations of ModelItem
        // the icon is used in the project overview
        modelItemIcons.put(Project.class, WsdlProject.ICON_NAME);
        modelItemIcons.put(TestSuite.class, WsdlTestSuite.ICON_NAME);
        modelItemIcons.put(TestCase.class, WsdlTestCase.ICON_NAME);
        modelItemIcons.put(LoadTest.class, WsdlLoadTest.ICON_NAME);
        modelItemIcons.put(MockService.class, WsdlMockService.ICON_NAME);
        modelItemIcons.put(MockResponse.class, WsdlMockResponse.ICON_NAME);
        modelItemIcons.put(MockOperation.class, WsdlMockOperation.ICON_NAME);
        modelItemIcons.put(RestMockService.class, RestMockService.ICON_NAME);
        modelItemIcons.put(RestMockAction.class, RestMockAction.getDefaultIcon());
        modelItemIcons.put(RestMockResponse.class, RestMockResponse.ICON_NAME);
        modelItemIcons.put(Operation.class, WsdlOperation.ICON_NAME);
        modelItemIcons.put(SecurityTest.class, SecurityTest.ICON_NAME);

        // the following use different icon files for the overview and in the tree
        modelItemIcons.put(TestStep.class, "/teststeps.gif");
        modelItemIcons.put(TestAssertion.class, "/assertions.png");
        modelItemIcons.put(Request.class, "/soap_request.png");
        modelItemIcons.put(Interface.class, "/interface.png");
    }

    public static ImageIcon getIcon(Class<? extends ModelItem> clazz) {
        if (modelItemIcons.containsKey(clazz)) {
            return UISupport.createImageIcon(modelItemIcons.get(clazz));
        }

        for (Class<?> iface : clazz.getInterfaces()) {
            if (modelItemIcons.containsKey(iface)) {
                return UISupport.createImageIcon(modelItemIcons.get(iface));
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static String getIconPath(Class<? extends ModelItem> clazz) {
        if (modelItemIcons.containsKey(clazz)) {
            return modelItemIcons.get(clazz);
        }

        for (Class<?> iface : clazz.getInterfaces()) {
            if (modelItemIcons.containsKey(iface)) {
                return modelItemIcons.get(iface);
            }
        }

        while (clazz.getSuperclass() != null && ModelItem.class.isAssignableFrom(clazz.getSuperclass())) {
            return getIconPath((Class<? extends ModelItem>) clazz.getSuperclass());
        }

        return null;
    }

    public static <T extends ModelItem> String getIconPath(T modelItem) {
        return getIconPath(modelItem.getClass());
    }

    public static <T extends ModelItem> ImageIcon getIcon(T modelItem) {
        return getIcon(modelItem.getClass());
    }
}
