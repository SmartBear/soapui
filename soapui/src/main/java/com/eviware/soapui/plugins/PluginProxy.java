package com.eviware.soapui.plugins;

import java.lang.annotation.Annotation;

/**
 * Interface exposing information about a proxied plugin class.
 */
public interface PluginProxy {

    <T extends Annotation> T getProxiedClassAnnotation(Class<T> annotationClass);

}
