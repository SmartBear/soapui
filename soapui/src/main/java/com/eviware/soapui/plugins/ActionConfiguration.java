/*
 *  SoapUI Pro, copyright (C) 2007-2014 smartbear.com
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
