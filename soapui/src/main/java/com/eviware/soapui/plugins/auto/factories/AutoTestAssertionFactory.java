package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.plugins.SoapUIFactory;
import com.eviware.soapui.plugins.auto.PluginTestAssertion;
//import com.google.inject.Inject;
//import com.google.inject.Injector;

/**
 * Created by ole on 15/06/14.
 */
public class AutoTestAssertionFactory extends AbstractTestAssertionFactory implements SoapUIFactory {
    private final String name;
    private final String description;
    private Class<WsdlMessageAssertion> testAssertionClass;
    private String category;
    //private Injector injector;

    public AutoTestAssertionFactory(PluginTestAssertion annotation, Class<WsdlMessageAssertion> testAssertionClass) {
        super(annotation.id(), annotation.label(), testAssertionClass);
        this.testAssertionClass = testAssertionClass;
        category = annotation.category();
        name = annotation.label();
        description = annotation.description();
    }
/*
    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    @Override
    public TestAssertion buildAssertion(TestAssertionConfig config, Assertable assertable) {
        TestAssertion assertion = super.buildAssertion(config, assertable);
        if (assertion != null && injector != null)
            injector.injectMembers(assertion);

        return assertion;
    }
*/

    @Override
    public Class<? extends WsdlMessageAssertion> getAssertionClassType() {
        return testAssertionClass;
    }

    @Override
    public AssertionListEntry getAssertionListEntry() {
        return new AssertionListEntry(getAssertionId(), getAssertionLabel(), description);
    }

    @Override
    public String getCategory() {
        return category;
    }
}
