
package com.eviware.soapui.impl.wsdl.loadtest;

import au.com.bytecode.opencsv.CSVReader;
import com.eviware.soapui.model.testsuite.BulkTestRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.impl.wsdl.support.AbstractTestRunner;
import com.eviware.soapui.support.types.StringToObjectMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WsdlBulkTestRunner extends AbstractTestRunner {
    private final static Logger log = LogManager.getLogger(WsdlLoadTestRunner.class);
    private WsdlBulkTest bulkTest;

    public WsdlBulkTestRunner(WsdlBulkTest bulkTest, StringToObjectMap context) {
        super(bulkTest, context);
        this.bulkTest = bulkTest;
    }

    public void start() {
        // Implementation for running bulk tests
    }

}
