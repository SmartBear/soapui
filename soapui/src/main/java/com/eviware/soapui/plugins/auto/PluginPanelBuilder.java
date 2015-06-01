package com.eviware.soapui.plugins.auto;

import com.eviware.soapui.model.ModelItem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@AutoFactory
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginPanelBuilder {
    Class<? extends ModelItem> targetModelItem();

    boolean moduleSpecific() default false;
}
