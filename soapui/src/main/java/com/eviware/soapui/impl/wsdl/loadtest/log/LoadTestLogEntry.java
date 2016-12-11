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

package com.eviware.soapui.impl.wsdl.loadtest.log;

import com.eviware.soapui.support.action.swing.ActionList;

import javax.swing.ImageIcon;
import java.io.IOException;

/**
 * An entry in the loadtest log
 *
 * @author Ole.Matzura
 */

public interface LoadTestLogEntry {
    public String getMessage();

    public long getTimeStamp();

    public String getType();

    public String getTargetStepName();

    public ActionList getActions();

    public ImageIcon getIcon();

    public boolean isError();

    public void discard();

    public boolean isDiscarded();

    public void exportToFile(String fileName) throws IOException;
}
