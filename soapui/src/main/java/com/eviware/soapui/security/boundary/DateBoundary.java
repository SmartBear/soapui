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

package com.eviware.soapui.security.boundary;

import java.text.SimpleDateFormat;

/**
 * @author nebojsa.tasic
 */
public class DateBoundary extends AbstractBoundary {

    private static final int OFFSET = 10;
    public static final String DATE_FORMAT = "yyyy-MM-dd'T00:00:00'";

    private static final ThreadLocal<SimpleDateFormat> simpleDateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DATE_FORMAT);
        }
    };

    @Override
    public String outOfBoundary(int restrictionAttribute, String value) {
        switch (restrictionAttribute) {
            case MAX_EXCLISIVE:
                return BoundaryUtils.createDate(value, (int) (Math.random() * OFFSET), simpleDateFormat.get());
            case MIN_EXCLISIVE:
                return BoundaryUtils.createDate(value, -(int) (Math.random() * OFFSET), simpleDateFormat.get());
            case MAX_INCLISIVE:
                return BoundaryUtils.createDate(value, (int) (Math.random() * OFFSET + 1), simpleDateFormat.get());
            case MIN_INCLISIVE:
                return BoundaryUtils.createDate(value, (-(int) (Math.random() * OFFSET) - 1), simpleDateFormat.get());
            default:
                return null;
        }
    }

}
