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

import com.eviware.soapui.SoapUI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author nebojsa.tasic
 */
public class BoundaryUtils {

    /**
     * create string of specified size from random characters specified by
     * availableValues
     *
     * @param availableValues
     * @param size
     * @return
     */
    public static String createCharacterArray(String availableValues, Integer size) {
        if (size == null) {
            SoapUI.log.error("size is not specified!");
            return null;
        }
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append(randomCharacter(availableValues));
        }
        return sb.toString();
    }

    /**
     * returns one random character from specified availableValues string
     *
     * @param availableValues
     * @return character
     */
    public static String randomCharacter(String availableValues) {
        int position = (int) (Math.random() * availableValues.length());
        return availableValues.substring(position, position + 1);
    }

    /**
     * creates date in string representation that is differs from restrictionDate
     * by daysOffset number of days
     *
     * @param restrictionDate
     * @param daysOffset
     * @return date
     */
    public static String createDate(String restrictionDate, int daysOffset, SimpleDateFormat format) {
        try {
            Date date = format.parse(restrictionDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_YEAR, daysOffset);
            return format.format(calendar.getTime());
        } catch (ParseException e) {
            SoapUI.logError(e, "date : '" + restrictionDate + "' is not in proper format: " + format.toPattern());
        }
        return null;
    }

    /**
     * creates time in string representation that is differs from minutesOffset
     * by minutesOffset number of minutes
     *
     * @param restrictionTime
     * @param minutesOffset
     * @return date
     */
    public static String createTime(String restrictionTime, int minutesOffset, SimpleDateFormat format) {
        try {
            Date date = format.parse(restrictionTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MINUTE, minutesOffset);
            return format.format(calendar.getTime());
        } catch (ParseException e) {
            SoapUI.logError(e, "time : '" + restrictionTime + "' is not in proper format: " + format.toPattern());
        }
        return null;
    }
}
