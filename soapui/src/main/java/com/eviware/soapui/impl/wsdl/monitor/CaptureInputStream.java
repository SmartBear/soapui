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

package com.eviware.soapui.impl.wsdl.monitor;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CaptureInputStream extends FilterInputStream {
    private final ByteArrayOutputStream capture = new ByteArrayOutputStream();
    private long maxData = 0;
    private boolean inCapture;

    public CaptureInputStream(InputStream in, long maxData) {
        super(in);
        this.maxData = maxData;
    }

    public CaptureInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        if (inCapture) {
            return super.read();
        } else {
            inCapture = true;
            int i = super.read();
            if (i != -1 && (maxData == 0 || capture.size() < maxData)) {
                capture.write(i);
            }
            inCapture = false;
            return i;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (inCapture) {
            return super.read(b);
        } else {
            inCapture = true;
            int i = super.read(b);
            if (i > 0) {
                if (maxData == 0) {
                    capture.write(b, 0, i);
                } else if (i > 0 && maxData > 0 && capture.size() < maxData) {
                    if (i + capture.size() < maxData) {
                        capture.write(b, 0, i);
                    } else {
                        capture.write(b, 0, (int) (maxData - capture.size()));
                    }
                }
            }
            inCapture = false;
            return i;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (inCapture) {
            return super.read(b, off, len);
        } else {
            inCapture = true;
            int i = super.read(b, off, len);
            if (i > 0) {
                if (maxData == 0) {
                    capture.write(b, off, i);
                } else if (i > 0 && maxData > 0 && capture.size() < maxData) {
                    if (i + capture.size() < maxData) {
                        capture.write(b, off, i);
                    } else {
                        capture.write(b, off, (int) (maxData - capture.size()));
                    }
                }
                inCapture = false;
            }

            return i;
        }
    }

    public byte[] getCapturedData() {
        return capture.toByteArray();
    }
}
