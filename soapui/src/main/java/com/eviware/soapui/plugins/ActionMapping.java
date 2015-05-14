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
