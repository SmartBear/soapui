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

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class InfiniteInputStream extends InputStream {
    private long bytesSent = 0;
    private long byteLimit;

    public InfiniteInputStream(long maxSize) {
        super();
        byteLimit = maxSize;
    }

    @Override
    public int read() throws IOException {
        if (bytesSent >= byteLimit) {
            return -1;
        }
        Random rnd = new Random();
        bytesSent++;
        return rnd.nextInt();
    }

}
