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

package com.eviware.x.form.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A field in an AForm
 *
 * @author ole.matzura
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AField {
    public enum AFieldType {
        BOOLEAN, STRING, FILE, FOLDER, FILE_OR_FOLDER, INT, ENUMERATION, PASSWORD, FILELIST, RADIOGROUP, STRINGAREA,
        MULTILIST, STRINGLIST, TABLE, ACTION, COMPONENT, SEPARATOR, INFORMATION, LABEL, RADIOGROUP_TOP_BUTTON, COMBOBOX
    }

    public String name() default "";

    public String description();

    public AFieldType type() default AFieldType.STRING;

    public String group() default "";

    public String[] values() default "";

    public String defaultValue() default "";

    boolean enabled() default true;
}
