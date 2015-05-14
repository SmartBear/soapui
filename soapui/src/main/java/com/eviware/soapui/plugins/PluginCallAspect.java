package com.eviware.soapui.plugins;

import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public final class PluginCallAspect {

    private static final Runnable noOp = new Runnable() {
        @Override
        public void run() {
        }
    };

    private static final AtomicReference<Runnable> onPluginCall = new AtomicReference<Runnable>(noOp);
    private static final AtomicReference<Runnable> afterPluginCall = new AtomicReference<Runnable>(noOp);

    private PluginCallAspect() {
        // static class
    }

    /**
     * Sets a Runnable to run before every plugin call.
     * <p/>
     * This method may only be called once.
     *
     * @param runnable execute before a plugin call
     * @throws java.lang.IllegalStateException if already set.
     */
    public static void runBeforePluginCall(Runnable runnable) {
        boolean ok = onPluginCall.compareAndSet(noOp, runnable);
        if (!ok) {
            throw new IllegalStateException("Already set");
        }
    }

    /**
     * Sets a Runnable to run after every plugin call.
     * <p/>
     * This method may only be called once.
     *
     * @param runnable execute after a plugin call
     * @throws java.lang.IllegalStateException if already set.
     */
    public static void runAfterPluginCall(Runnable runnable) {
        boolean ok = afterPluginCall.compareAndSet(noOp, runnable);
        if (!ok) {
            throw new IllegalStateException("Already set");
        }
    }

    /**
     * @return the onPluginCall Runnable, or a no-op Runnable if not set.
     */
    public static Runnable getOnPluginCall() {
        return onPluginCall.get();
    }


    /**
     * @return the afterPluginCall Runnable, or a no-op Runnable if not set.
     */
    public static Runnable getAfterPluginCall() {
        return afterPluginCall.get();
    }

}
