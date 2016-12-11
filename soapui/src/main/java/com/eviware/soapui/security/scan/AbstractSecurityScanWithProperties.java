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

package com.eviware.soapui.security.scan;

import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.support.XPathReference;
import com.eviware.soapui.model.support.XPathReferenceContainer;
import com.eviware.soapui.model.support.XPathReferenceImpl;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.support.SecurityCheckedParameterHolder;
import com.eviware.soapui.security.support.SecurityCheckedParameterImpl;
import com.eviware.soapui.support.types.StringToStringMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * These are for Security Scans that mutate parameters.
 *
 * @author robert
 */
public abstract class AbstractSecurityScanWithProperties extends AbstractSecurityScan implements
        XPathReferenceContainer {
    public static final String SECURITY_CHANGED_PARAMETERS = "SecurityChangedParameters";
    private SecurityCheckedParameterHolder parameterHolder;

    public AbstractSecurityScanWithProperties(TestStep testStep, SecurityScanConfig config, ModelItem parent,
                                              String icon) {
        super(testStep, config, parent, icon);

        setParameterHolder(new SecurityCheckedParameterHolder(this, getConfig().getCheckedParameters()));
    }

    public SecurityCheckedParameterHolder getParameterHolder() {
        return this.parameterHolder;
    }

    protected void setParameterHolder(SecurityCheckedParameterHolder parameterHolder) {
        this.parameterHolder = parameterHolder;
    }

    @Override
    public void copyConfig(SecurityScanConfig config) {
        super.copyConfig(config);
        getConfig().setCheckedParameters(config.getCheckedParameters());
        if (parameterHolder != null) {
            parameterHolder.release();
        }

        parameterHolder = new SecurityCheckedParameterHolder(this, config.getCheckedParameters());
    }

    public XPathReference[] getXPathReferences() {
        List<XPathReference> result = new ArrayList<XPathReference>();

        for (SecurityCheckedParameter param : getParameterHolder().getParameterList()) {
            TestStep t = getTestStep();
            if (t instanceof WsdlTestRequestStep) {
                if (param != null) {
                    result.add(new XPathReferenceImpl("SecurityScan Parameter " + param.getLabel() + " in \""
                            + getTestStep().getName() + "\"", ((WsdlTestRequestStep) t).getOperation(), true, param,
                            "xpath"));
                }
            }
        }

        return result.toArray(new XPathReference[result.size()]);
    }

    @Override
    public void updateSecurityConfig(SecurityScanConfig config) {
        super.updateSecurityConfig(config);

        if (getParameterHolder() != null && getConfig().getCheckedParameters() != null) {
            getParameterHolder().updateConfig(config.getCheckedParameters());
        }
    }

    public SecurityCheckedParameter getParameterAt(int i) {
        if (!getParameterHolder().getParameterList().isEmpty() && getParameterHolder().getParameterList().size() > i) {
            return getParameterHolder().getParameterList().get(i);
        } else {
            return null;
        }
    }

    public SecurityCheckedParameter getParameterByLabel(String label) {
        return parameterHolder.getParametarByLabel(label);
    }

    public boolean importParameter(SecurityCheckedParameter source, boolean overwrite, String newLabel) {
        // TODO double check if this needs to return newly added parameter
        // also maybe add label checking to holder.addParam...
        // and use overwrite also
        SecurityCheckedParameterImpl param = (SecurityCheckedParameterImpl) getParameterHolder().getParametarByLabel(
                newLabel);
        if (param != null) {
            if (overwrite) {
                param.setName(source.getName());
                param.setXpath(source.getXpath());
                param.setChecked(source.isChecked());
                return true;
            } else {
                return false;
            }
        } else {
            return getParameterHolder().addParameter(newLabel, source.getName(), source.getXpath(), source.isChecked());
        }
    }

    protected void createMessageExchange(StringToStringMap updatedParams, MessageExchange message,
                                         SecurityTestRunContext context) {
        for (Map.Entry<String, String> param : updatedParams.entrySet()) {
            String value = context.expand(param.getValue());
            updatedParams.put(param.getKey(), value);
        }
        message.getProperties().put(SECURITY_CHANGED_PARAMETERS, updatedParams.toXml());
        getSecurityScanRequestResult().setMessageExchange(message);
    }

    @Override
    public void release() {
        if (parameterHolder != null) {
            parameterHolder.release();
        }
        super.release();
    }
}
