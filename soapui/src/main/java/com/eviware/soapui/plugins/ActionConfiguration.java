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

import com.eviware.soapui.model.ModelItem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides additional Action configuration to action classes in a plugin.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionConfiguration {

    String actionGroup() default "";

    String beforeAction() default "";

    String afterAction() default "";

    public String description() default "";

    public boolean defaultAction() default false;

    public String iconPath() default "";

    public String keyStroke() default "";

    public boolean separatorBefore() default false;

    public boolean separatorAfter() default false;

    public boolean isToolbarAction() default false;

    public ToolbarPosition toolbarPosition() default ToolbarPosition.NONE;

    public String toolbarIcon() default "";

    Class targetType() default ModelItem.class;

}
