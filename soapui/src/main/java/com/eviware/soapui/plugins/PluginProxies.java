package com.eviware.soapui.plugins;

import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JComponent;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashSet;

/**
 * Creates proxies for code loaded from a plugin, ensuring that the class loader of the proxied class will be used
 * as context class loader when methods from plugins are being invoked, and that the JIDE Charts license is cleared
 * before plugin code
 */
public class PluginProxies {

    private static final Method OBJECT_EQUALS_METHOD = getObjectEqualsMethod();
    private static final Logger log = LoggerFactory.getLogger(PluginProxies.class);

    private static Method getObjectEqualsMethod() {
        try {
            return Object.class.getMethod("equals", Object.class);
        } catch (NoSuchMethodException e) {
            // shouldn't happen. Really.
            throw new Error("Object.equals() not found!");
        }
    }

    public static <T> T proxyIfApplicable(T delegate) {
        if (loadedFromPluginJar(delegate)) {
            return createProxyFor(delegate);
        } else {
            return delegate;
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T createProxyFor(T delegate) {

        if (delegate instanceof JComponent) {
            log.warn("Can't proxy JComponent derived classes");
            return delegate;
        }

        Collection<Class> interfaces = ClassUtils.getAllInterfaces(delegate.getClass());
        if (interfaces.isEmpty()) {
            // this shouldn't really happen, unless reflection is being used in some odd way
            log.warn("Can't proxy instance of {} because the class doesn't implement any interfaces", delegate.getClass());
            return delegate;
        }
        interfaces.add(PluginProxy.class);
        return (T) Proxy.newProxyInstance(PluginProxies.class.getClassLoader(),
                interfaces.toArray(new Class[interfaces.size()]), new DelegatingHandler<T>(delegate));
    }

    public static <T> Collection<T> proxyInstancesWhereApplicable(Collection<T> instancesToProxy) {
        Collection<T> proxiedInstances = new HashSet<T>();
        for (T instance : instancesToProxy) {
            proxiedInstances.add(proxyIfApplicable(instance));
        }
        return proxiedInstances;
    }

    public static <T extends Annotation> T getAnnotation(Object possiblyProxiedObject, Class<T> annotationClass) {
        if (possiblyProxiedObject == null) {
            return null;
        }

        if (possiblyProxiedObject instanceof Class) {
            return (T) ((Class) possiblyProxiedObject).getAnnotation(annotationClass);
        }

        T annotation = possiblyProxiedObject.getClass().getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }

        return (possiblyProxiedObject instanceof PluginProxy) ?
                ((PluginProxy) possiblyProxiedObject).getProxiedClassAnnotation(annotationClass) : null;
    }

    private static <T> boolean loadedFromPluginJar(T delegate) {
        return delegate != null && delegate.getClass().getClassLoader() instanceof PluginClassLoader;
    }

    private static class DelegatingHandler<T> implements InvocationHandler {

        private T innerObject;

        public DelegatingHandler(T innerObject) {
            this.innerObject = innerObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.equals(OBJECT_EQUALS_METHOD)) {
                return equalsInternal(args[0]);
            } else if (method.getName().equals("hashCode")) {
                return innerObject.hashCode();
            } else if (method.getDeclaringClass().equals(PluginProxy.class) &&
                    method.getName().equals("getProxiedClassAnnotation")) {
                return innerObject.getClass().getAnnotation((Class<Annotation>) args[0]);
            }
            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
            PluginCallAspect.getOnPluginCall().run();
            if (shouldUsePluginClassloaderFor(method)) {
                Thread.currentThread().setContextClassLoader(innerObject.getClass().getClassLoader());
            }
            try {
                Object returnValue = method.invoke(innerObject, args);
                return proxyIfApplicable(returnValue);
            } finally {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
                PluginCallAspect.getAfterPluginCall().run();
            }
        }

        private boolean shouldUsePluginClassloaderFor(Method method) {
            try {
                return method.getAnnotation(UsePluginClassloader.class) != null ||
                        method.getDeclaringClass().getAnnotation(UsePluginClassloader.class) != null ||
                        innerObject.getClass().getAnnotation(UsePluginClassloader.class) != null ||
                        innerObject.getClass().getMethod(method.getName(), method.getParameterTypes()).getAnnotation(UsePluginClassloader.class) != null;
            } catch (NoSuchMethodException e) {
                return false;
            }
        }

        private boolean equalsInternal(Object other) {
            if (other == null) {
                return false;
            }
            Object actualOtherObject = null;
            if (Proxy.isProxyClass(other.getClass())) {
                InvocationHandler handler = Proxy.getInvocationHandler(other);
                if (handler instanceof DelegatingHandler) {
                    actualOtherObject = ((DelegatingHandler) handler).innerObject;
                }
            } else {
                actualOtherObject = other;
            }
            return innerObject.equals(actualOtherObject);
        }
    }
}
