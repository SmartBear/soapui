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

public interface Environment {

    public void setProject(Project project);

    public Project getProject();

    public void release();

    public Service addNewService(String name, ServiceConfig.Type.Enum serviceType);

    public void removeService(Service service);

    public String getName();

    public Property addNewProperty(String name, String value);

    public void removeProperty(Property property);

    public void changePropertyName(String name, String value);

    public void moveProperty(String name, int idx);

    public void setName(String name);

}
