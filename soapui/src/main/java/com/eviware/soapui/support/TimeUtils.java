/*
 * Copyright 2004-2014 SmartBear Software
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

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public final class TimeUtils {

    private static final PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
            .printZeroNever()
            .appendMonths().appendSuffix(" month", " months").appendSeparator(", ")
            .appendWeeks().appendSuffix(" week", " weeks").appendSeparator(", ")
            .appendDays().appendSuffix(" day", " days").appendSeparator(", ")
            .printZeroAlways()
            .minimumPrintedDigits(2)
            .appendHours().appendSeparator(":")
            .appendMinutes().appendSeparator(":")
            .appendSeconds()
            .toFormatter();

    private TimeUtils() {
        // only for static use
    }

    public static long getCurrentTimeInSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Formats a time duration given by the start and end instants in milliseconds.
     *
     * @param start
     * @param end
     * @return formatted String duration
     */
    public static String formatTimeDuration(long start, long end) {
        final Period period = new Period(start, end);
        return periodFormatter.print(period);
    }

}
