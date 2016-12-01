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

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.impl.wsdl.teststeps.AbstractPathPropertySupport;

public class PathPropertyExternalDependency implements ExternalDependency {
    private final AbstractPathPropertySupport pathProperty;
    private final Type type;

    public PathPropertyExternalDependency(AbstractPathPropertySupport pathProperty) {
        this(pathProperty, Type.FILE);
    }

    public PathPropertyExternalDependency(AbstractPathPropertySupport pathProperty, Type type) {
        this.pathProperty = pathProperty;
        this.type = type;
    }

    @Override
    public String getPath() {
        return pathProperty.expand();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void updatePath(String path) {
        pathProperty.set(path, true);
    }
}
