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

package com.eviware.soapui.model.environment;

import com.eviware.soapui.config.ServiceConfig;
import com.eviware.soapui.model.project.Project;

public class DefaultEnvironment implements Environment {

    public static final String NAME = "Default";

    private DefaultEnvironment() {
    }

    private static class DefaultEnvironmentHolder {
        public static final DefaultEnvironment instance = new DefaultEnvironment();
    }

    public static DefaultEnvironment getInstance() {
        return DefaultEnvironmentHolder.instance;
    }

    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof DefaultEnvironment);
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    public void setProject(Project project) {
    }

    public Project getProject() {
        return null;
    }

    public void release() {
    }

    public Service addNewService(String name, ServiceConfig.Type.Enum serviceType) {
        return null;
    }

    public void removeService(Service service) {
    }

    public Property addNewProperty(String name, String value) {
        return null;
    }

    public void removeProperty(Property property) {
    }

    public void changePropertyName(String name, String value) {
    }

    public void moveProperty(String name, int idx) {
    }

    @Override
    public void setName(String name) {
        // TODO Auto-generated method stub
    }

}
