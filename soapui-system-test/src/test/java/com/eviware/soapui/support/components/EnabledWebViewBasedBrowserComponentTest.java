package com.eviware.soapui.support.components;

import com.smartbear.soapui.utils.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.swing.*;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

@Category(IntegrationTest.class)
public class EnabledWebViewBasedBrowserComponentTest {
    private static final String MEMBER_NAME = "member";
    private static final int TIMEOUT = 10;

    private final CountDownLatch latch = new CountDownLatch(1);

    @Test
    public void sucessfullCallbackWhenClickingOnAnElement() throws InterruptedException, URISyntaxException {
        final String TEST_STARTER_PAGE_URL = EnabledWebViewBasedBrowserComponentTest.class
                .getResource("/starter-pages/starter-page-with-an-single-action-button.html").toURI().toString();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                WebViewBasedBrowserComponent browserComponent = new EnabledWebViewBasedBrowserComponent(true
                        , WebViewBasedBrowserComponent.PopupStrategy.INTERNAL_BROWSER_REUSE_WINDOW);
                browserComponent.addJavaScriptEventHandler(MEMBER_NAME, new JavaScriptCallback());
                browserComponent.navigate(TEST_STARTER_PAGE_URL);
            }
        });
        /* Wait for the browser to initialize and click on the HTML element programmatically.
        If we don't get a callback within a resonable amount of time, fail the test */
        assertThat(latch.await(TIMEOUT, TimeUnit.SECONDS), is(true));
    }

    public class JavaScriptCallback {
        public void call() {
            latch.countDown();
        }
    }
}