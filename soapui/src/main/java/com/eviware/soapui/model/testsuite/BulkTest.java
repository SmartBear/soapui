
package com.eviware.soapui.model.testsuite;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;

import java.util.List;

public interface BulkTest extends ModelItem, TestRunnable {
    WsdlTestCase getTestCase();
    List<String> getDataFile();
}
