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

package com.eviware.x.form;

/**
 * @author lars
 */
public abstract class WizardPage {
    private String name;
    private String description;

    public WizardPage(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean canGoBack() {
        return false;
    }

    /**
     * Initialize the page. Note that this can be called multiple times if going
     * Back and Next.
     *
     * @return true if the page was initialized ok, false to end the wizard.
     * @throws Exception
     */
    public abstract boolean init() throws Exception;

    /**
     * @return true if the page finished ok, false to end the wizard.
     * @throws Exception
     */
    public abstract boolean run() throws Exception;
}
