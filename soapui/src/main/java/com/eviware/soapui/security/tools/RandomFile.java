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

import com.eviware.soapui.support.UISupport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class RandomFile {

    private File file;
    private long length;

    private final Random random = new Random();

    public RandomFile(long length, String name, String contentType) throws IOException {
        this.length = length;
        file = new File(name);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public File next() throws IOException {

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(file));
            long used = 0;

            while (used <= length) {
                used++;
                out.write(random.nextInt());
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            UISupport.showErrorMessage(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }

        return file;
    }

}
