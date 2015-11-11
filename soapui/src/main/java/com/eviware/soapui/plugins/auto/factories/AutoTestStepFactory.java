package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.plugins.SoapUIFactory;
import com.eviware.soapui.plugins.auto.PluginTestStep;
import com.eviware.soapui.support.StringUtils;
/*
 * import com.google.inject.Inject; import com.google.inject.Injector;
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class AutoTestStepFactory extends WsdlTestStepFactory implements SoapUIFactory {
    private PluginTestStep annotation;
    private Class<TestStep> testStepClass;

    public AutoTestStepFactory(PluginTestStep annotation, Class<TestStep> testStepClass) {
        super(annotation.typeName(), annotation.name(), annotation.description(), annotation.iconPath());
        this.annotation = annotation;
        this.testStepClass = testStepClass;
    }

    @Override
    public Class<?> getFactoryType() {
        return WsdlTestStepFactory.class;
    }

    @Override
    public WsdlTestStep buildTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {

        WsdlTestStep result = null;

        try {
            Method method = testStepClass.getMethod("buildTestStep", WsdlTestCase.class, TestStepConfig.class, Boolean.class);
            result = (WsdlTestStep) method.invoke(testCase, config, forLoadTest);
        } catch (NoSuchMethodException e) {

            try {
                Constructor constructor = testStepClass.getConstructor(WsdlTestCase.class, TestStepConfig.class, boolean.class);
                result = (WsdlTestStep) constructor.newInstance(testCase, config, forLoadTest);
            } catch (Exception e1) {
                SoapUI.logError(e);
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return result;
    }

    @Override
    public TestStepConfig createNewTestStep(WsdlTestCase testCase, String name) {

        try {
            Method method = testStepClass.getMethod("createNewTestStep", WsdlTestCase.class, String.class);
            return (TestStepConfig) method.invoke(testCase, name);
        } catch (NoSuchMethodException e) {
            TestStepConfig config = TestStepConfig.Factory.newInstance();
            config.setType(annotation.typeName());
            config.setName(StringUtils.hasContent(name) ? name : annotation.name());
            return config;
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return null;
    }

    @Override
    public boolean canCreate() {

        try {
            Method method = testStepClass.getMethod("canCreate");
            return Boolean.valueOf(method.invoke(new Object[0]).toString());
        } catch (NoSuchMethodException e) {
            return true;
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return true;
    }
}
