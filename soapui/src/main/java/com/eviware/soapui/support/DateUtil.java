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

package com.eviware.soapui.support;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    private static final ThreadLocal<SimpleDateFormat> SIMPLE_FORMAT = createThreadLocal("HH:mm:ss");

    private static final ThreadLocal<SimpleDateFormat> SIMPLE_FORMAT_WITH_MILLIS = createThreadLocal("HH:mm:ss.SSS");

    private static final ThreadLocal<SimpleDateFormat> FULL_FORMAT = createThreadLocal("yyyy-MM-dd HH:mm:ss");

    private static final ThreadLocal<SimpleDateFormat> EXTRA_FULL_FORMAT = createThreadLocal("yyyy-MM-dd HH:mm:ss.SSS");

    private static ThreadLocal<SimpleDateFormat> createThreadLocal(final String format) {
        return new ThreadLocal<SimpleDateFormat>() {
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat(format);
            }

            ;
        };
    }

    /**
     * Formats: HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String formatSimple(Date date) {
        return SIMPLE_FORMAT.get().format(date);
    }

    /**
     * Formats: yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String formatFull(Date date) {
        return FULL_FORMAT.get().format(date);
    }

    /**
     * Formats: yyyy-MM-dd HH:mm:ss.SSS
     *
     * @param date
     * @return
     */
    public static String formatExtraFull(Date date) {
        return EXTRA_FULL_FORMAT.get().format(date);
    }

    /**
     * Formats: HH:mm:ss.SSS
     *
     * @param date
     * @return
     */
    public static String formatSimpleWithMillis(Date date) {
        return SIMPLE_FORMAT_WITH_MILLIS.get().format(date);
    }
}
