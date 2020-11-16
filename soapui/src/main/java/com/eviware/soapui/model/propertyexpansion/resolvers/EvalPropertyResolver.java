/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.model.propertyexpansion.resolvers;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.teststeps.TestRequest;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockRunContext;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.SamplerTestStep;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.scripting.ScriptEnginePool;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.types.StringToObjectMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class EvalPropertyResolver implements PropertyResolver {
    private Logger log = LogManager.getLogger(EvalPropertyResolver.class);
    private Map<String, ScriptEnginePool> scriptEnginePools = new HashMap<String, ScriptEnginePool>();

    public String resolveProperty(PropertyExpansionContext context, String name, boolean globalOverride) {
        if (name.length() == 0 || name.charAt(0) != '=') {
            return null;
        }

        name = name.substring(1);

        StringToObjectMap objects = new StringToObjectMap(context.getProperties());
        objects.put("context", context);
        objects.put("log", SoapUI.ensureGroovyLog());

        if (context instanceof TestCaseRunContext) {
            TestCaseRunContext testCaseRunContext = (TestCaseRunContext) context;
            objects.put("testRunner", testCaseRunContext.getTestRunner());

            objects.put("testStep", testCaseRunContext.getCurrentStep());

            if (testCaseRunContext.getCurrentStep() instanceof SamplerTestStep) {
                objects.put("request", ((SamplerTestStep) testCaseRunContext.getCurrentStep()).getTestRequest());
            }
        }

        if (context instanceof LoadTestRunContext) {
            objects.put("loadTestRunner", ((LoadTestRunContext) context).getLoadTestRunner());
        }

        if (context instanceof MockRunContext) {
            objects.put("mockRunner", ((MockRunContext) context).getMockRunner());
        }

        ModelItem modelItem = context.getModelItem();
        if (modelItem instanceof TestCase) {
            objects.put("testCase", modelItem);

            objects.put("testSuite", ((TestCase) modelItem).getTestSuite());
            objects.put("project", ((TestCase) modelItem).getTestSuite().getProject());
        } else if (modelItem instanceof TestStep) {
            objects.put("testStep", modelItem);

            if (modelItem instanceof SamplerTestStep) {
                objects.put("request", ((SamplerTestStep) modelItem).getTestRequest());
            }

            objects.put("testCase", ((TestStep) modelItem).getTestCase());
            objects.put("testSuite", ((TestStep) modelItem).getTestCase().getTestSuite());
            objects.put("project", ((TestStep) modelItem).getTestCase().getTestSuite().getProject());
        } else if (modelItem instanceof TestSuite) {
            objects.put("testSuite", modelItem);
            objects.put("project", ((TestSuite) modelItem).getProject());
        }
        if (modelItem instanceof LoadTest) {
            objects.put("loadTest", modelItem);
            objects.put("testCase", ((LoadTest) modelItem).getTestCase());
            objects.put("testSuite", ((LoadTest) modelItem).getTestCase().getTestSuite());
            objects.put("project", ((LoadTest) modelItem).getTestCase().getTestSuite().getProject());
        } else if (modelItem instanceof Project) {
            objects.put("project", modelItem);
        } else if (modelItem instanceof MockService) {
            objects.put("mockService", modelItem);
            objects.put("project", ((MockService) modelItem).getProject());
        } else if (modelItem instanceof MockOperation) {
            objects.put("mockOperation", modelItem);
            objects.put("mockService", ((MockOperation) modelItem).getMockService());
            objects.put("project", ((MockOperation) modelItem).getMockService().getProject());
        } else if (modelItem instanceof MockResponse) {
            objects.put("mockResponse", modelItem);
            objects.put("mockOperation", ((MockResponse) modelItem).getMockOperation());
            objects.put("mockService", ((MockResponse) modelItem).getMockOperation().getMockService());
            objects.put("project", ((MockResponse) modelItem).getMockOperation().getMockService().getProject());
        } else if (modelItem instanceof Request) {
            objects.put("request", modelItem);

            if (modelItem instanceof TestRequest) {
                objects.put("testStep", ((TestRequest) modelItem).getTestStep());
                objects.put("testCase", ((TestRequest) modelItem).getTestStep().getTestCase());
                objects.put("testSuite", ((TestRequest) modelItem).getTestStep().getTestCase().getTestSuite());
                objects.put("project", ((TestRequest) modelItem).getTestStep().getTestCase().getTestSuite()
                        .getProject());
            }
        } else if (modelItem instanceof Operation) {
            objects.put("operation", modelItem);
        } else if (modelItem instanceof Interface) {
            objects.put("interface", modelItem);
        } else if (modelItem instanceof SecurityTest) {
            objects.put("securityTest", modelItem);
        }

        if (modelItem != null) {
            objects.put("modelItem", modelItem);
        }

        return doEval(name, modelItem, objects);
    }

    private String doEval(String name, ModelItem modelItem, StringToObjectMap objects) {
        String engineId = SoapUIScriptEngineRegistry.getScriptEngineId(modelItem);

        synchronized (this) {
            if (!scriptEnginePools.containsKey(engineId)) {
                scriptEnginePools.put(engineId, new ScriptEnginePool(engineId));
            }
        }

        ScriptEnginePool scriptEnginePool = scriptEnginePools.get(engineId);
        SoapUIScriptEngine scriptEngine = scriptEnginePool.getScriptEngine();
        try {
            scriptEngine.setScript(name);
            for (Map.Entry<String, Object> entry : objects.entrySet()) {
                scriptEngine.setVariable(entry.getKey(), entry.getValue());
            }

            Object result = scriptEngine.run();
            return result == null ? null : result.toString();
        } catch (Throwable e) {
            log.error("Error evaluating script", e);
            return e.getMessage();
        } finally {
            scriptEngine.clearVariables();
            scriptEnginePool.returnScriptEngine(scriptEngine);
        }
    }
}
