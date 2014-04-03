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

package com.eviware.soapui.model.support;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.testsuite.TestProperty;

public class TestPropertyUtils {
    private static boolean ascending;

    public static int saveTo(TestPropertyHolder propertyHolder, String fileName) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(fileName));

        for (TestProperty prop : propertyHolder.getPropertyList()) {
            writer.print(prop.getName());
            writer.print('=');
            String value = prop.getValue();
            if (value == null) {
                value = "";
            }

            String[] lines = value.split("\n");
            for (int c = 0; c < lines.length; c++) {
                if (c > 0) {
                    writer.println("\\");
                }
                writer.print(lines[c]);
            }

            writer.println();
        }

        writer.close();
        return propertyHolder.getPropertyCount();
    }

    public synchronized static void sortProperties(MutableTestPropertyHolder holder) {
        ascending = false;

        String[] names = holder.getPropertyNames();

        quicksort(holder, 0, holder.getPropertyCount() - 1);
        if (Arrays.equals(names, holder.getPropertyNames())) {
            ascending = true;
            quicksort(holder, 0, holder.getPropertyCount() - 1);
        }
    }

    private static void quicksort(MutableTestPropertyHolder array, int lo, int hi) {
        if (hi > lo) {
            int partitionPivotIndex = (int) (Math.random() * (hi - lo) + lo);
            int newPivotIndex = partition(array, lo, hi, partitionPivotIndex);
            quicksort(array, lo, newPivotIndex - 1);
            quicksort(array, newPivotIndex + 1, hi);
        }
        // return (T[]) array;
    }

    private static int partition(MutableTestPropertyHolder array, int lo, int hi, int pivotIndex) {
        TestProperty pivotValue = array.getPropertyAt(pivotIndex);

        swap(array, pivotIndex, hi); // send to the back

        int index = lo;

        for (int i = lo; i < hi; i++) {
            if (ascending) {
                if ((array.getPropertyAt(i).getName().toUpperCase().compareTo(pivotValue.getName().toUpperCase()) >= 0)) {
                    swap(array, i, index);
                    index++;
                }
            } else {
                if ((array.getPropertyAt(i).getName().toUpperCase().compareTo(pivotValue.getName().toUpperCase()) <= 0)) {
                    swap(array, i, index);
                    index++;
                }
            }
        }

        swap(array, hi, index);

        return index;
    }

    private static void swap(MutableTestPropertyHolder array, int i, int j) {
        String prop1 = array.getPropertyAt(i).getName();
        String prop2 = array.getPropertyAt(j).getName();

        array.moveProperty(prop1, j);
        array.moveProperty(prop2, i);

        //
        // T temp = array[i];
        // array[i] = array[j];
        // array[j] = temp;
    }
}
