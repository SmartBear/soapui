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

package com.eviware.soapui.impl.wsdl.support.soap;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.support.BindingImporter;
import com.eviware.soapui.impl.wsdl.support.policy.PolicyUtils;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import java.text.Collator;
import java.util.Comparator;

public abstract class AbstractSoapBindingImporter implements BindingImporter {

    protected static final class BindingOperationComparator implements Comparator<BindingOperation> {
        public int compare(BindingOperation o1, BindingOperation o2) {
            return Collator.getInstance().compare(o1.getOperation().getName(), o2.getOperation().getName());
        }
    }

    protected void initWsAddressing(Binding binding, WsdlInterface iface, Definition def) throws Exception {
        iface.setWsaVersion(WsdlUtils.getUsingAddressing(binding));
        // if (iface.getWsaVersion().equals(WsaVersionTypeConfig.NONE.toString()))
        // {
        iface.processPolicy(PolicyUtils.getAttachedPolicy(binding, def));
        // }
    }

    public AbstractSoapBindingImporter() {
        super();
    }

}
