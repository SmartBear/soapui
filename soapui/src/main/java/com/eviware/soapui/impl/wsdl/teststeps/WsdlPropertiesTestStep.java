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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.config.PropertiesStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.ExternalDependency;
import com.eviware.soapui.impl.wsdl.support.PathPropertyExternalDependency;
import com.eviware.soapui.impl.wsdl.support.XmlBeansPropertiesTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.types.StringList;

import javax.swing.ImageIcon;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * TestStep that holds an arbitrary number of custom properties
 *
 * @author ole.matzura
 */

public class WsdlPropertiesTestStep extends WsdlTestStep implements MutableTestPropertyHolder {
    private PropertiesStepConfig propertiesStepConfig;
    private ImageIcon okIcon;
    private ImageIcon failedIcon;
    private XmlBeansPropertiesTestPropertyHolder propertyHolderSupport;
    private BeanPathPropertySupport sourceProperty;
    private BeanPathPropertySupport targetProperty;

    public static final String SOURCE_PROPERTY = WsdlPropertiesTestStep.class.getName() + "@source";
    public static final String TARGET_PROPERTY = WsdlPropertiesTestStep.class.getName() + "@target";

    public WsdlPropertiesTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {
        super(testCase, config, true, forLoadTest);

        if (!forLoadTest) {
            okIcon = UISupport.createImageIcon("/properties_step.png");
            failedIcon = UISupport.createImageIcon("/failed_properties_step.png");

            setIcon(okIcon);
        }

        if (config.getConfig() == null) {
            propertiesStepConfig = (PropertiesStepConfig) config.addNewConfig().changeType(PropertiesStepConfig.type);
            propertiesStepConfig.addNewProperties();
            propertiesStepConfig.setCreateMissingOnLoad(true);
        } else {
            propertiesStepConfig = (PropertiesStepConfig) config.getConfig().changeType(PropertiesStepConfig.type);
            if (!propertiesStepConfig.isSetProperties()) {
                propertiesStepConfig.addNewProperties();
            }

            if (!propertiesStepConfig.isSetSaveFirst()) {
                propertiesStepConfig.setSaveFirst(true);
            }
        }

        propertyHolderSupport = new XmlBeansPropertiesTestPropertyHolder(this, propertiesStepConfig.getProperties());
        sourceProperty = new BeanPathPropertySupport(this, propertiesStepConfig, "source");
        targetProperty = new BeanPathPropertySupport(this, propertiesStepConfig, "target");
    }

    public TestStepResult run(TestCaseRunner testRunner, TestCaseRunContext testRunContext) {
        WsdlTestStepResult result = new WsdlTestStepResult(this);

        if (okIcon != null) {
            setIcon(okIcon);
        }

        result.setStatus(TestStepStatus.OK);
        result.startTimer();

        if (isSaveFirst()) {
            saveDuringRun(result, testRunContext);
        }

        String source = sourceProperty.expand(testRunContext);
        if (StringUtils.hasContent(source)) {
            try {
                int cnt = loadProperties(source, isCreateMissingOnLoad());

                result.setStatus(TestStepStatus.OK);
                result.addMessage("Loaded " + cnt + " properties from [" + source + "]");
            } catch (IOException e) {
                result.stopTimer();
                result.addMessage("Failed to load properties from [" + source + "]");
                result.setStatus(TestStepStatus.FAILED);
                result.setError(e);

                if (failedIcon != null) {
                    setIcon(failedIcon);
                }
            }
        }

        if (!isSaveFirst()) {
            saveDuringRun(result, testRunContext);
        }

        result.stopTimer();

        return result;
    }

    private boolean saveDuringRun(WsdlTestStepResult result, TestCaseRunContext context) {
        String target = targetProperty.expand(context);
        if (StringUtils.hasContent(target)) {
            try {
                int cnt = saveProperties(target);

                result.setStatus(TestStepStatus.OK);
                result.addMessage("Saved " + cnt + " properties to [" + target + "]");
            } catch (IOException e) {
                result.stopTimer();
                result.addMessage("Failed to save properties to [" + target + "]");
                result.setStatus(TestStepStatus.FAILED);
                result.setError(e);

                if (failedIcon != null) {
                    setIcon(failedIcon);
                }

                return false;
            }
        }

        return true;
    }

    private int saveProperties(String target) throws IOException {
        return propertyHolderSupport.saveTo(System.getProperty(target, target));
    }

    private int loadProperties(String source, boolean createMissing) throws IOException {
        // override methods so propertynames are returned in readorder
        java.util.Properties props = new java.util.Properties() {
            public StringList names = new StringList();

            @Override
            public synchronized Object put(Object key, Object value) {
                names.add(key.toString());
                return super.put(key, value);
            }

            @Override
            public Enumeration<?> propertyNames() {
                return Collections.enumeration(names);
            }
        };

        InputStream in = getPropertiesInputStream(source);
        props.load(in);
        in.close();

        int cnt = 0;
        Enumeration<?> names = props.propertyNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement().toString();
            TestProperty property = getProperty(name);
            if (property != null) {
                property.setValue(props.get(name).toString());
                cnt++;
            } else if (createMissing) {
                addProperty(name).setValue(props.get(name).toString());
                cnt++;
            }
        }

        return cnt;
    }

    private InputStream getPropertiesInputStream(String source) throws IOException {
        String fileProperty = System.getProperty(source);
        if (fileProperty != null) {
            source = fileProperty;
        }

        URL url;

        try {
            url = new URL(source);
        } catch (MalformedURLException e) {
            url = new URL("file:" + source);
        }

        return url.openStream();
    }

    public TestProperty getTestStepPropertyAt(int index) {
        return propertyHolderSupport.getPropertyAt(index);
    }

    public int getStepPropertyCount() {
        return propertyHolderSupport.getPropertyCount();
    }

    public String getSource() {
        return sourceProperty.get();
    }

    public void setSource(String source) {
        sourceProperty.set(source, true);
    }

    public String getTarget() {
        return targetProperty.get();
    }

    public String getLabel() {
        String str = super.getName() + " (" + getPropertyCount() + ")";

        if (isDisabled()) {
            str += " (disabled)";
        }

        return str;
    }

    public void setTarget(String target) {
        targetProperty.set(target, true);
    }

    public void resetConfigOnMove(TestStepConfig config) {
        super.resetConfigOnMove(config);

        propertiesStepConfig = (PropertiesStepConfig) config.getConfig().changeType(PropertiesStepConfig.type);
        propertyHolderSupport.resetPropertiesConfig(propertiesStepConfig.getProperties());
        sourceProperty.setConfig(propertiesStepConfig);
        targetProperty.setConfig(propertiesStepConfig);
    }

    public int loadProperties(boolean createMissing) throws IOException {
        return loadProperties(sourceProperty.expand(), createMissing);
    }

    public int saveProperties() throws IOException {
        String target = PropertyExpander.expandProperties(this, targetProperty.expand());
        return saveProperties(target);
    }

    public boolean isCreateMissingOnLoad() {
        return propertiesStepConfig.getCreateMissingOnLoad();
    }

    public void setCreateMissingOnLoad(boolean b) {
        propertiesStepConfig.setCreateMissingOnLoad(b);
    }

    public boolean isSaveFirst() {
        return propertiesStepConfig.getSaveFirst();
    }

    public void setSaveFirst(boolean b) {
        propertiesStepConfig.setSaveFirst(b);
    }

    public boolean isDiscardValuesOnSave() {
        return propertiesStepConfig.getDiscardValuesOnSave();
    }

    public void setDiscardValuesOnSave(boolean b) {
        propertiesStepConfig.setDiscardValuesOnSave(b);
    }

    public void setPropertyValue(String name, String value) {
        if (isCreateMissingOnLoad() && getProperty(name) == null) {
            addProperty(name);
        }

        propertyHolderSupport.setPropertyValue(name, value);
    }

    @Override
    public void beforeSave() {
        super.beforeSave();

        if (isDiscardValuesOnSave()) {
            clearPropertyValues();
        }
    }

    public void clearPropertyValues() {
        for (TestProperty property : propertyHolderSupport.getProperties().values()) {
            property.setValue(null);
        }
    }

    public boolean renameProperty(String name, String newName) {
        return PropertyExpansionUtils.renameProperty(propertyHolderSupport.getProperty(name), newName, getTestCase()) != null;
    }

    public TestProperty addProperty(String name) {
        String oldLabel = getLabel();

        TestProperty property = propertyHolderSupport.addProperty(name);
        notifyPropertyChanged(WsdlTestStep.LABEL_PROPERTY, oldLabel, getLabel());

        return property;
    }

    public void addTestPropertyListener(TestPropertyListener listener) {
        propertyHolderSupport.addTestPropertyListener(listener);
    }

    public Map<String, TestProperty> getProperties() {
        return propertyHolderSupport.getProperties();
    }

    public TestProperty getProperty(String name) {
        return propertyHolderSupport.getProperty(name);
    }

    public TestProperty getPropertyAt(int index) {
        return propertyHolderSupport.getPropertyAt(index);
    }

    public List<TestProperty> getPropertyList() {
        return propertyHolderSupport.getPropertyList();
    }

    public int getPropertyCount() {
        return propertyHolderSupport.getPropertyCount();
    }

    public String[] getPropertyNames() {
        return propertyHolderSupport.getPropertyNames();
    }

    public String getPropertyValue(String name) {
        return propertyHolderSupport.getPropertyValue(name);
    }

    public TestProperty removeProperty(String propertyName) {
        String oldLabel = getLabel();

        TestProperty result = propertyHolderSupport.removeProperty(propertyName);
        notifyPropertyChanged(WsdlTestStep.LABEL_PROPERTY, oldLabel, getLabel());
        return result;
    }

    public void removeTestPropertyListener(TestPropertyListener listener) {
        propertyHolderSupport.removeTestPropertyListener(listener);
    }

    public boolean hasProperty(String name) {
        return propertyHolderSupport.hasProperty(name);
    }

    public void addExternalDependency(List<ExternalDependency> dependencies) {
        super.addExternalDependencies(dependencies);
        dependencies.add(new PathPropertyExternalDependency(targetProperty));
        dependencies.add(new PathPropertyExternalDependency(sourceProperty));
    }

    @Override
    public void resolve(ResolveContext<?> context) {
        super.resolve(context);

        targetProperty.resolveFile(context, "Missing target property file", "properties",
                "Properties Files (*.properties)", true);
        sourceProperty.resolveFile(context, "Missing source property file", "properties",
                "Properties Files (*.properties)", true);
    }

    public void moveProperty(String propertyName, int targetIndex) {
        propertyHolderSupport.moveProperty(propertyName, targetIndex);
    }

    public String getSource(boolean expand) {
        return expand ? sourceProperty.expand() : getSource();
    }

    public String getTarget(boolean expand) {
        return expand ? targetProperty.expand() : getTarget();
    }
}
