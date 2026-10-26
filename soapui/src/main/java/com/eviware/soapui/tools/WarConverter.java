/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
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

package com.eviware.soapui.tools;

import org.apache.tomcat.jakartaee.EESpecProfiles;
import org.apache.tomcat.jakartaee.Migration;

import java.io.File;
import java.io.IOException;

public class WarConverter {

    private WarConverter() {
    }

    public static void convert(File source, File destination) throws IOException {
        Migration migration = new Migration();
        migration.setEESpecProfile(EESpecProfiles.TOMCAT);
        migration.setSource(source);
        migration.setDestination(destination);
        try {
            migration.execute();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}
