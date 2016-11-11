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

package com.eviware.soapui.plugins;

import org.apache.commons.lang.ObjectUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by ole on 20/08/14.
 */

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionMapping {
    String actionId() default "";

    Class<? extends Object> actionClass() default ObjectUtils.Null.class;

    String keyStroke() default "";

    String iconPath() default "";

    Type type() default Type.ACTION;

    String name() default "";

    String param() default "";

    String description() default "";

    String groupId() default "";

    boolean isToolbarAction() default false;

    int toolbarIndex() default 0;

    public enum Type {ACTION, GROUP, INSERT, SEPARATOR}
}
