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

package com.eviware.soapui.impl.wsdl.teststeps.assertions;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.plugins.SoapUIFactory;
import com.eviware.soapui.support.ClassUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractTestAssertionFactory implements TestAssertionFactory, SoapUIFactory {
    private final String id;
    private final String label;
    private final Class<? extends TestAssertion> assertionClass;
    private final List<Class<? extends ModelItem>> targetClasses = new ArrayList<Class<? extends ModelItem>>();

    public AbstractTestAssertionFactory(String id, String label, Class<? extends TestAssertion> assertionClass) {
        this.id = id;
        this.label = label;
        this.assertionClass = assertionClass;
    }

    @SuppressWarnings("unchecked")
    public AbstractTestAssertionFactory(String id, String label, Class<? extends TestAssertion> assertionClass,
                                        Class<? extends ModelItem> targetClass) {
        this(id, label, assertionClass, new Class[]{targetClass});
    }

    public AbstractTestAssertionFactory(String id, String label, Class<? extends TestAssertion> assertionClass,
                                        Class<? extends ModelItem>[] targetClasses) {
        this.id = id;
        this.label = label;
        this.assertionClass = assertionClass;
        for (Class<? extends ModelItem> clazz : targetClasses) {
            this.targetClasses.add(clazz);
        }
    }

    public String getAssertionId() {
        return id;
    }

    public String getAssertionLabel() {
        return label;
    }

    public boolean canAssert(Assertable assertable) {
    	List<?> classes = ClassUtils.getSuperInterfaces(assertionClass);

        List<Class<?>> classList = ClassUtils.getImplementedAndExtendedClasses(assertable);
        if (!targetClasses.isEmpty() && Collections.disjoint(classList, targetClasses)) {
            return false;
        }

        if (assertable.getAssertableType() == AssertableType.BOTH) {
            return true;
        }

        if (assertable.getAssertableType() == AssertableType.REQUEST
                && classes.contains(com.eviware.soapui.model.testsuite.RequestAssertion.class)) {
            return true;
        } else if (assertable.getAssertableType() == AssertableType.RESPONSE
                && classes.contains(com.eviware.soapui.model.testsuite.ResponseAssertion.class)) {
            return true;
        }

        return false;
    }

    /*
     * by default assertions can not be applied to properties each assertion
     * needs to specify otherwise
     */
    public boolean canAssert(TestPropertyHolder modelItem, String property) {
        return false;
    }

    public TestAssertion buildAssertion(TestAssertionConfig config, Assertable assertable) {
        try {
            Constructor<? extends TestAssertion> ctor = assertionClass.getConstructor(new Class[]{
                    TestAssertionConfig.class, Assertable.class});

            return ctor.newInstance(config, assertable);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Class<?> getFactoryType() {
        return TestAssertionFactory.class;
    }
}
