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

package com.eviware.soapui.security.support;

import com.eviware.soapui.config.CheckedParameterConfig;
import com.eviware.soapui.config.CheckedParametersListConfig;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.security.SecurityScanParameterHolderListener;
import com.eviware.soapui.model.testsuite.TestPropertyListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holder for SecurityScanPameters, which are request parameters on which
 * security scan is applied.
 *
 * @author robert
 */
public class SecurityCheckedParameterHolder extends SecurityScanParameterListenerAdapter implements
        TestPropertyListener {

    private SecurityScan securityCheck;
    private CheckedParametersListConfig paramsConfig;

    private List<SecurityCheckedParameter> params = new ArrayList<SecurityCheckedParameter>();
    private Map<String, SecurityCheckedParameter> paramsMap = new HashMap<String, SecurityCheckedParameter>();

    private Set<SecurityScanParameterHolderListener> listeners = new HashSet<SecurityScanParameterHolderListener>();

    public SecurityCheckedParameterHolder(SecurityScan securityCheck, CheckedParametersListConfig checkedPameters) {
        this.securityCheck = securityCheck;
        this.paramsConfig = checkedPameters;

        for (CheckedParameterConfig param : paramsConfig.getParametersList()) {
            addParameter(param);
        }

        securityCheck.getTestStep().addTestPropertyListener(this);
    }

    public void addParameterListener(SecurityScanParameterHolderListener listener) {
        listeners.add(listener);
    }

    public void removeParameterListener(SecurityScanParameterHolderListener listener) {
        listeners.remove(listener);
    }

    public void updateConfig(CheckedParametersListConfig config) {
        paramsConfig = config;

        List<CheckedParameterConfig> paramsList = config.getParametersList();
        for (int c = 0; c < paramsList.size(); c++) {
            if (params.get(c) instanceof SecurityCheckedParameterImpl) {
                ((SecurityCheckedParameterImpl) params.get(c)).setConfig(paramsList.get(c));
            }
        }
    }

    SecurityCheckedParameter addParameter(CheckedParameterConfig param) {
        SecurityCheckedParameter result = new SecurityCheckedParameterImpl(param);
        params.add(result);
        paramsMap.put(result.getLabel().toUpperCase(), result);

        fireParameterAdded(result);

        return result;
    }

    public boolean addParameter(String label, String name, String xpath, boolean used) {
        if (paramsMap.get(label.toUpperCase()) != null) {
            return false;
        }

        CheckedParameterConfig newParameterConfig = paramsConfig.addNewParameters();
        SecurityCheckedParameterImpl newParameter = new SecurityCheckedParameterImpl(newParameterConfig);
        newParameter.setLabel(label);
        newParameter.setName(name);
        newParameter.setXpath(xpath);
        newParameter.setChecked(used);
        params.add(newParameter);
        paramsMap.put(newParameter.getLabel().toUpperCase(), newParameter);

        fireParameterAdded(newParameter);

        return true;
    }

    public void removeParameter(SecurityCheckedParameter parameter) {

        int index = params.indexOf(parameter);
        params.remove(parameter);
        paramsMap.remove(parameter.getLabel().toUpperCase());

        paramsConfig.removeParameters(index);

        fireParameterRemoved(parameter);
    }

    public void fireParameterAdded(SecurityCheckedParameter parameter) {
        for (SecurityScanParameterHolderListener listener : listeners) {
            listener.parameterAdded(parameter);
        }
    }

    public void fireParameterRemoved(SecurityCheckedParameter parameter) {
        for (SecurityScanParameterHolderListener listener : listeners) {
            listener.parameterRemoved(parameter);
        }
    }

    public List<SecurityCheckedParameter> getParameterList() {
        return params;
    }

    public String[] getParameterLabels() {
        String[] labels = new String[params.size()];
        for (int c = 0; c < labels.length; c++) {
            labels[c] = params.get(c).getLabel();
        }

        return labels;
    }

    /*
     * Need to keep track on parameter label change.
     *
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.security.support.SecurityScanParameterListenerAdapter
     * #parameterLabelChanged
     * (com.eviware.soapui.model.security.SecurityCheckedParameter,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void parameterLabelChanged(SecurityCheckedParameter parameter, String oldLabel, String newLabel) {
        SecurityCheckedParameter param = paramsMap.get(oldLabel);
        paramsMap.remove(oldLabel);
        paramsMap.put(newLabel, param);
    }

    /**
     * This method returns parameter based on its label.
     *
     * @param paramLabel
     * @return parameter
     */
    public SecurityCheckedParameter getParametarByLabel(String paramLabel) {
        for (SecurityCheckedParameter param : params) {
            if (param.getLabel().equals(paramLabel)) {
                return param;
            }
        }
        return null;
    }

    /**
     * This method returns parameter based on its name.
     *
     * @param paramName
     * @return parameter
     */
    public List<SecurityCheckedParameter> getParametarsByName(String paramName) {
        List<SecurityCheckedParameter> paramsList = new ArrayList<SecurityCheckedParameter>();
        for (SecurityCheckedParameter param : params) {
            if (param.getName().equals(paramName)) {
                paramsList.add(param);
            }
        }
        return paramsList;
    }

    public void removeParameters(int[] selected) {
        ArrayList<SecurityCheckedParameter> paramsToRemove = new ArrayList<SecurityCheckedParameter>();
        for (int index : selected) {
            paramsToRemove.add(params.get(index));
        }

        for (SecurityCheckedParameter param : paramsToRemove) {
            removeParameter(param);
        }
    }

    @Override
    public void propertyAdded(String name) {
        // TODO Auto-generated method stub
        // we do not care for this
    }

    @Override
    public void propertyMoved(String name, int oldIndex, int newIndex) {
        // TODO Auto-generated method stub
        // we do not care about this, we keep order by label
    }

    @Override
    public void propertyRemoved(String name) {
        ArrayList<SecurityCheckedParameter> parameterToRemove = new ArrayList<SecurityCheckedParameter>();
        for (SecurityCheckedParameter param : params) {
            if (param.getName().equals(name)) {
                parameterToRemove.add(param);
            }
        }
        for (SecurityCheckedParameter param : parameterToRemove) {
            removeParameter(param);
        }
    }

    @Override
    public void propertyRenamed(String oldName, String newName) {
        ArrayList<SecurityCheckedParameter> parameterToRemove = new ArrayList<SecurityCheckedParameter>();
        for (SecurityCheckedParameter param : params) {
            if (param.getName().equals(oldName)) {
                parameterToRemove.add(param);
            }
        }
        for (SecurityCheckedParameter param : parameterToRemove) {
            ((SecurityCheckedParameterImpl) param).setName(newName);
        }

    }

    @Override
    public void propertyValueChanged(String name, String oldValue, String newValue) {
        // TODO Auto-generated method stub
        // we do not cate for this
    }

    public void release() {
        securityCheck.getTestStep().removeTestPropertyListener(this);
    }
}
