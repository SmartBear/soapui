/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.support.action;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.plugins.ActionConfiguration;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.action.support.DefaultSoapUIActionGroup;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SoapUIActionRegistryTest {

    private SoapUIActionRegistry registry = new SoapUIActionRegistry(null);

    @Test
    public void addsActionsInCorrectPositions() throws Exception {
        TestActionGroup actionGroup = new TestActionGroup();
        registry.addActionGroup(actionGroup, "TestActionGroup");
        registry.addAction("Dummy2", new DummyAction2());
        registry.addAction("Dummy4", new DummyAction4());
        registry.addAction("Dummy3", new DummyAction3());
        registry.addAction("Dummy1", new DummyAction1());
        SoapUIActionGroup<ModelItem> retrievedGroup = registry.getActionGroup(TestActionGroup.ID);
        SoapUIActionMappingList<ModelItem> actionMappings = retrievedGroup.getActionMappings(null);
        assertThat(actionMappings.getMappingIndex("DummyAction1"), is(0));
        assertThat(actionMappings.getMappingIndex("DummyAction2"), is(1));
        assertThat(actionMappings.getMappingIndex("DummyAction3"), is(2));
        assertThat(actionMappings.getMappingIndex("DummyAction4"), is(3));
    }

    private class TestActionGroup extends DefaultSoapUIActionGroup {

        public static final String ID = "TestActionGroup";

        public TestActionGroup() {
            super("TestActionGroup", "TestActionGroup");
        }

    }

    private abstract class DummyAction extends AbstractSoapUIAction<WsdlProject> {

        public DummyAction(String id) {
            super(id);
        }

        @Override
        public void perform(WsdlProject target, Object param) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    @ActionConfiguration(actionGroup = TestActionGroup.class, groupId = "com.smartbear.tests", name = "DummyAction1", beforeAction = "DummyAction2")
    private class DummyAction1 extends DummyAction {

        public DummyAction1() {
            super("DummyAction1");
        }
    }

    @ActionConfiguration(actionGroup = TestActionGroup.class, groupId = "com.smartbear.tests", name = "DummyAction2")
    private class DummyAction2 extends DummyAction {

        public DummyAction2() {
            super("DummyAction2");
        }
    }

    @ActionConfiguration(actionGroup = TestActionGroup.class, groupId = "com.smartbear.tests", name = "DummyAction3", afterAction = "DummyAction2")
    private class DummyAction3 extends DummyAction {

        public DummyAction3() {
            super("DummyAction3");
        }
    }

    @ActionConfiguration(actionGroup = TestActionGroup.class, groupId = "com.smartbear.tests", name = "DummyAction4")
    private class DummyAction4 extends DummyAction {

        public DummyAction4() {
            super("DummyAction4");
        }
    }
}
