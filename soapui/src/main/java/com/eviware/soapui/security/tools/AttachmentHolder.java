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

package com.eviware.soapui.security.tools;

import com.eviware.soapui.config.MaliciousAttachmentConfig;

import java.util.ArrayList;
import java.util.List;

public class AttachmentHolder {

    List<MaliciousAttachmentConfig> list;

    public void addElement(MaliciousAttachmentConfig config) {
        if (list == null) {
            list = new ArrayList<MaliciousAttachmentConfig>();
        }

        list.add(config);
    }

    public void removeElement(int i) {
        if (list != null) {
            list.remove(i);
        }
    }

    public int size() {
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }

    public void clear() {
        if (list != null) {
            list.clear();
        }
    }

    public List<MaliciousAttachmentConfig> getList() {
        return list;
    }

}
