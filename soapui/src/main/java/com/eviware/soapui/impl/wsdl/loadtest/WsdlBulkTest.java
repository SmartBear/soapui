
package com.eviware.soapui.impl.wsdl.loadtest;

import com.eviware.soapui.config.BulkTestConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.testsuite.BulkTest;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.support.types.StringToObjectMap;

import java.util.ArrayList;
import java.util.List;

public class WsdlBulkTest extends AbstractWsdlModelItem<BulkTestConfig> implements BulkTest {

    private WsdlTestCase testCase;
    private List<String> dataFile;
    private WsdlBulkTestRunner runner;

    public WsdlBulkTest(WsdlTestCase testCase, BulkTestConfig config) {
        super(config, testCase, "/bulkTest.png");
        this.testCase = testCase;
    }

    @Override
    public WsdlTestCase getTestCase() {
        return testCase;
    }

    @Override
    public TestRunner run(StringToObjectMap context, boolean async) {
        runner = new WsdlBulkTestRunner(this, context);
        runner.start();
        return runner;
    }

    @Override
    public List<String> getDataFile() {
        if (this.dataFile == null) {
            this.dataFile = new ArrayList<>();
        }
        return this.dataFile;
    }

    public boolean isRunning() {
        return runner != null && runner.isRunning();
    }

    public WsdlBulkTestRunner getRunner() {
        return runner;
    }
}
