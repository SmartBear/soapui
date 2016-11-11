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

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;

import static org.junit.Assert.assertTrue;

public class DateBoundaryTest {

    DateTimeBoundary dateBoundary;
    String today;

    @Before
    public void setUp() throws Exception {
        today = DateTimeBoundary.simpleDateFormat.get().format(Calendar.getInstance().getTime());
        dateBoundary = new DateTimeBoundary();
    }

    @Test
    public void testOutOfBoundaryMinExclusive() throws ParseException {
        String outOfBoundaryDate = dateBoundary.outOfBoundary(Boundary.MIN_EXCLISIVE, today);
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTime(DateTimeBoundary.simpleDateFormat.get().parse(outOfBoundaryDate));
        calendar2.setTime(DateTimeBoundary.simpleDateFormat.get().parse(today));
        assertTrue(calendar1.before(calendar2) || calendar1.equals(calendar2));
    }

    @Test
    public void testOutOfBoundaryMaxExclusive() throws ParseException {
        String outOfBoundaryDate = dateBoundary.outOfBoundary(Boundary.MAX_EXCLISIVE, today);
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTime(DateTimeBoundary.simpleDateFormat.get().parse(outOfBoundaryDate));
        calendar2.setTime(DateTimeBoundary.simpleDateFormat.get().parse(today));
        assertTrue(calendar1.after(calendar2) || calendar1.equals(calendar2));
    }

    @Test
    public void testOutOfBoundaryMinInclusive() throws ParseException {
        String outOfBoundaryDate = dateBoundary.outOfBoundary(Boundary.MIN_INCLISIVE, today);
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTime(DateTimeBoundary.simpleDateFormat.get().parse(outOfBoundaryDate));
        calendar2.setTime(DateTimeBoundary.simpleDateFormat.get().parse(today));
        assertTrue(calendar1.before(calendar2));
    }

    @Test
    public void testOutOfBoundaryMaxInclusive() throws ParseException {
        String outOfBoundaryDate = dateBoundary.outOfBoundary(Boundary.MAX_INCLISIVE, today);
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTime(DateTimeBoundary.simpleDateFormat.get().parse(outOfBoundaryDate));
        calendar2.setTime(DateTimeBoundary.simpleDateFormat.get().parse(today));
        assertTrue(calendar1.after(calendar2));
    }

}
