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

package com.eviware.soapui.impl.wsdl.panels.assertions;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.recent.RecentAssertionHandler;
import com.eviware.soapui.model.testsuite.Assertable;

import java.util.LinkedHashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class AssertionCategoryMapping {
    public final static String VALIDATE_RESPONSE_CONTENT_CATEGORY = "Property Content";
    public final static String STATUS_CATEGORY = "Compliance, Status and Standards";
    public final static String SCRIPT_CATEGORY = "Script";
    public final static String SLA_CATEGORY = "SLA";
    public final static String JMS_CATEGORY = "JMS";
    public final static String SECURITY_CATEGORY = "Security";
    public final static String RECENTLY_USED = "Recently used";
    public static final String JDBC_CATEGORY = "JDBC";
    //this category shouldn't be shown anywhere as such
    //group assertion is not assertion but rather container for a group of assertions
    //therefore this should not be included in getAssertionCategories()
    public static final String GROUPING = "GROUPING";

    public static String[] getAssertionCategories() {
        return new String[]{VALIDATE_RESPONSE_CONTENT_CATEGORY, STATUS_CATEGORY, SCRIPT_CATEGORY, SLA_CATEGORY,
                JMS_CATEGORY, JDBC_CATEGORY, SECURITY_CATEGORY};
    }

    /**
     * @param assertable
     * @param recentAssertionHandler
     * @return Set of Recently used assertion if @param assertable is not null
     *         only recently used assertions applicable to the @param assertable
     *         will be included if @param assertable is null all recently used
     *         assertions will be included
     */
    private static SortedSet<AssertionListEntry> createRecentlyUsedSet(Assertable assertable,
                                                                       RecentAssertionHandler recentAssertionHandler) {
        SortedSet<AssertionListEntry> recentlyUsedSet = new TreeSet<AssertionListEntry>();

        for (String name : recentAssertionHandler.get()) {
            String type = recentAssertionHandler.getAssertionTypeByName(name);

            if (type != null) {
                if (assertable == null || recentAssertionHandler.canAssert(type, assertable)) {
                    recentlyUsedSet.add(recentAssertionHandler.getAssertionListEntry(type));
                }
            }
        }
        return recentlyUsedSet;
    }

    /**
     * @param assertable
     * @param recentAssertionHandler
     * @return assertion categories mapped with assertions in exact category if @param
     *         assertable is not null only assertions for specific @param
     *         assertable will be included if @param assertable is null all
     *         assertions are included
     */
    public static LinkedHashMap<String, SortedSet<AssertionListEntry>> getCategoriesAssertionsMap(
            Assertable assertable, RecentAssertionHandler recentAssertionHandler) {
        LinkedHashMap<String, SortedSet<AssertionListEntry>> categoriesAssertionsMap = new LinkedHashMap<String, SortedSet<AssertionListEntry>>();

        SortedSet<AssertionListEntry> recentlyUsedSet = createRecentlyUsedSet(assertable, recentAssertionHandler);

        if (recentlyUsedSet.size() > 0) {
            categoriesAssertionsMap.put(RECENTLY_USED, recentlyUsedSet);
        }
        //		TestAssertionRegistry.getInstance().addCategoriesAssertionsMap( assertable, categoriesAssertionsMap );
        TestAssertionRegistry.getInstance().addAllCategoriesMap(categoriesAssertionsMap);
        return categoriesAssertionsMap;
    }

}
