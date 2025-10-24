
package com.eviware.soapui.impl.wsdl.panels.testcase;

import com.eviware.soapui.impl.wsdl.loadtest.WsdlBulkTest;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import com.eviware.soapui.ui.desktop.DesktopPanelFactory;

public class WsdlBulkTestPanelBuilder extends DesktopPanelFactory<WsdlBulkTest> {
    @Override
    public DesktopPanel buildDesktopPanel(WsdlBulkTest bulkTest) {
        return new WsdlBulkTestDesktopPanel(bulkTest);
    }

    @Override
    public Class<WsdlBulkTest> getTargetModelItem() {
        return WsdlBulkTest.class;
    }
}
