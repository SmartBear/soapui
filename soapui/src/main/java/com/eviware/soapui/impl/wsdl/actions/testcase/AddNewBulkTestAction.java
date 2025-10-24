
package com.eviware.soapui.impl.wsdl.actions.testcase;

import com.eviware.soapui.impl.wsdl.loadtest.WsdlBulkTest;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class AddNewBulkTestAction extends AbstractSoapUIAction<WsdlTestCase> {
    public static final String SOAPUI_ACTION_ID = "AddNewBulkTestAction";

    public AddNewBulkTestAction() {
        super("New BulkTest", "Creates a new BulkTest for this TestCase");
    }

    public void perform(WsdlTestCase testCase, Object param) {
        String name = UISupport.prompt("Specify name of BulkTest", "New BulkTest",
                "BulkTest " + (testCase.getLoadTestCount() + 1));
        if (name == null) {
            return;
        }

        WsdlBulkTest bulkTest = testCase.addNewBulkTest(name);
        UISupport.selectAndShow(bulkTest);
    }
}
