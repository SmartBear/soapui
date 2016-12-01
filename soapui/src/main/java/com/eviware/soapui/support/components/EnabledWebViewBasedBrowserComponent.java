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

package com.eviware.soapui.support.components;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.actions.oauth.BrowserListener;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.xml.XmlUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import netscape.javascript.JSObject;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.HeadlessException;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class EnabledWebViewBasedBrowserComponent implements WebViewBasedBrowserComponent {
    public static final String CHARSET_PATTERN = "(.+)(;\\s*charset=)(.+)";
    public static final String DEFAULT_ERROR_PAGE = "<html><body><h1>The page could not be loaded</h1></body></html>";
    private Pattern charsetFinderPattern = Pattern.compile(CHARSET_PATTERN);

    private JPanel panel = new JPanel(new BorderLayout());
    private String errorPage;
    private boolean showingErrorPage;
    public String url;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private java.util.List<BrowserListener> listeners = new ArrayList<BrowserListener>();

    public WebView webView;
    private WebViewNavigationBar navigationBar;
    private String lastLocation;
    private Set<BrowserWindow> browserWindows = new HashSet<BrowserWindow>();

    private JFXPanel browserPanel;
    private PopupStrategy popupStrategy;

    EnabledWebViewBasedBrowserComponent(boolean addNavigationBar, PopupStrategy popupStrategy) {
        this.popupStrategy = popupStrategy;
        initializeWebView(addNavigationBar);
    }

    @Override
    public Component getComponent() {
        return panel;
    }

    private void initializeWebView(boolean addNavigationBar) {
        if (addNavigationBar) {
            navigationBar = new WebViewNavigationBar();
            panel.add(navigationBar.getComponent(), BorderLayout.NORTH);
        }

        final JFXPanel browserPanel = new JFXPanel();
        panel.add(browserPanel, BorderLayout.CENTER);

        WebViewInitialization webViewInitialization = new WebViewInitialization(browserPanel);
        if (Platform.isFxApplicationThread()) {
            webViewInitialization.run();
        } else {
            Platform.runLater(webViewInitialization);
        }
        Runnable runnable = new Runnable() {
            public void run() {
                if (navigationBar != null) {
                    navigationBar.focusUrlField();
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    private String readDocumentAsString() throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(getWebEngine().getDocument()),
                new StreamResult(stringWriter));

        return stringWriter.getBuffer().toString().replaceAll("\n|\r", "");
    }

    private void addKeyboardFocusManager(final JFXPanel browserPanel) {
        KeyboardFocusManager kfm = DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager();
        kfm.addKeyEventDispatcher(new KeyEventDispatcher() {
                                      @Override
                                      public boolean dispatchKeyEvent(KeyEvent e) {
                                          if (DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == browserPanel) {
                                              if (e.getID() == KeyEvent.KEY_TYPED && e.getKeyChar() == 10) {
                                                  e.setKeyChar((char) 13);
                                              }
                                          }
                                          return false;
                                      }
                                  }
        );
    }

    @Override
    public void executeJavaScript(final String script) {
        Platform.runLater(new Runnable() {
            public void run() {
                try {
                    webView.getEngine().executeScript(script);
                    for (BrowserListener listener : listeners) {
                        listener.javaScriptExecuted(script, null, null);
                    }
                } catch (Exception e) {
                    SoapUI.log.warn("Error executing JavaScript [" + script + "]", e);
                    for (BrowserListener listener : listeners) {
                        listener.javaScriptExecuted(script, lastLocation, e);
                    }
                }
            }
        });
    }

    @Override
    public void addJavaScriptEventHandler(final String memberName, final Object eventHandler) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                getWebEngine().getLoadWorker().stateProperty().addListener(
                        new ChangeListener<Worker.State>() {
                            public void changed(ObservableValue observableValue, Worker.State oldState, Worker.State newState) {
                                if (newState == Worker.State.SUCCEEDED) {
                                    JSObject window = (JSObject) getWebEngine().executeScript("window");
                                    window.setMember(memberName, eventHandler);
                                }
                            }
                        }
                );
            }
        });
    }

    @Override
    public void close(boolean cascade) {
        if (cascade) {
            for (Iterator<BrowserWindow> iterator = browserWindows.iterator(); iterator.hasNext(); ) {
                iterator.next().close();
                iterator.remove();
            }
        }

        for (BrowserListener listener : listeners) {
            listener.browserClosed();
        }
        release();
    }

    private void release() {
        setContent("");
        browserPanel.setScene(null);
    }

    @Override
    public void setContent(final String contentAsString, final String contentType) {
        if (SoapUI.isBrowserDisabled()) {
            return;
        }
        Platform.runLater(new Runnable() {
            public void run() {

                getWebEngine().loadContent(contentAsString, removeCharsetFrom(contentType));
            }
        });
    }

    private String removeCharsetFrom(String contentType) {
        Matcher matcher = charsetFinderPattern.matcher(contentType);
        return matcher.matches() ? matcher.group(1) : contentType;
    }

    @Override
    public void setContent(final String contentAsString) {
        if (SoapUI.isBrowserDisabled()) {
            return;
        }
        Platform.runLater(new Runnable() {
            public void run() {
                getWebEngine().loadContent(contentAsString);
            }
        });
        pcs.firePropertyChange("content", null, contentAsString);
    }

    private WebEngine getWebEngine() {
        return webView.getEngine();
    }

    public String getContent() {
        return webView == null ? null : XmlUtils.serialize(getWebEngine().getDocument());
    }


    public String getUrl() {
        return url;
    }


    public void setErrorPage(String errorPage) {
        this.errorPage = errorPage;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }


    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }

    @Override
    public void navigate(final String url) {
        navigate(url, DEFAULT_ERROR_PAGE);
    }

    public void navigate(final String url, String errorPage) {
        if (SoapUI.isBrowserDisabled()) {
            return;
        }
        setErrorPage(errorPage);

        this.url = url;

        Platform.runLater(new Runnable() {
            public void run() {
                getWebEngine().load(url);
            }
        });

    }

    @Override
    public void addBrowserStateListener(BrowserListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeBrowserStateListener(BrowserListener listener) {
        listeners.remove(listener);
    }

	/*
     Class used to open a new browser window, used in the popup handler of the component.
	 */

    private class BrowserWindow extends JFrame {

        private final EnabledWebViewBasedBrowserComponent browser;

        private BrowserWindow(PopupFeatures popupFeatures) throws HeadlessException {
            super("Browser");
            setIconImages(SoapUI.getFrameIcons());
            browser = new EnabledWebViewBasedBrowserComponent(popupFeatures.hasToolbar(), popupStrategy);
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(browser.getComponent());
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    browser.close(false);
                }
            });
        }

        public void close() {
            setVisible(false);
            dispose();
            browser.close(true);
            browser.release();
        }

    }

	/*
     The task to initialize the Java FX WebView component. Will be run synchronously if we're already on the
	Java FX event thread, asynchronously if we aren't.
	 */

    private class WebViewInitialization implements Runnable {

        public WebViewInitialization(JFXPanel browserPanel) {
            EnabledWebViewBasedBrowserComponent.this.browserPanel = browserPanel;
        }

        public void run() {
            webView = new WebView();

            createPopupHandler();
            listenForLocationChanges();
            listenForStateChanges();

            if (navigationBar != null) {
                navigationBar.initialize(getWebEngine(), EnabledWebViewBasedBrowserComponent.this);
            }
            browserPanel.setScene(createJfxScene());
            addKeyboardFocusManager(browserPanel);
        }

        private Scene createJfxScene() {
            Group jfxComponentGroup = new Group();
            Scene scene = new Scene(jfxComponentGroup);
            webView.prefWidthProperty().bind(scene.widthProperty());
            webView.prefHeightProperty().bind(scene.heightProperty());
            jfxComponentGroup.getChildren().add(webView);
            return scene;
        }

        private void createPopupHandler() {
            switch (popupStrategy) {
                case INTERNAL_BROWSER_NEW_WINDOW:
                    webView.getEngine().setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() {
                        @Override
                        public WebEngine call(PopupFeatures pf) {
                            BrowserWindow popupWindow = new BrowserWindow(pf);
                            browserWindows.add(popupWindow);
                            popupWindow.setSize(800, 600);
                            popupWindow.setVisible(true);
                            return popupWindow.browser.getWebEngine();
                        }
                    });
                    break;
                case EXTERNAL_BROWSER:
                    webView.getEngine().setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() {
                        @Override
                        public WebEngine call(PopupFeatures pf) {
                            final WebEngine webEngine = new WebEngine();
                            webEngine.locationProperty().addListener(new ChangeListener<String>() {
                                @Override
                                public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                                    observableValue.removeListener(this);
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            webEngine.loadContent("");
                                        }
                                    });
                                    Tools.openURL(newValue);
                                }
                            });
                            return webEngine;
                        }
                    });
                    break;
                case DISABLED:
                    webView.getEngine().setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() {
                        @Override
                        public WebEngine call(PopupFeatures pf) {
                            return null;
                        }
                    });
                    break;
                case INTERNAL_BROWSER_REUSE_WINDOW:
                default:
                    break;
            }
        }

        private void listenForLocationChanges() {
            webView.getEngine().locationProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String oldLocation,
                                    String newLocation) {
                    lastLocation = newLocation;
                    for (BrowserListener listener : listeners) {
                        listener.locationChanged(newLocation);
                    }
                }
            });
        }

        private void listenForStateChanges() {
            webView.getEngine().getLoadWorker().stateProperty().addListener(
                    new ChangeListener<Worker.State>() {
                        @Override
                        public void changed(ObservableValue value, Worker.State oldState, Worker.State newState) {
                            if (newState == Worker.State.SUCCEEDED) {
                                try {
                                    if (getWebEngine().getDocument() != null) {
                                        String output = readDocumentAsString();
                                        for (BrowserListener listener : listeners) {
                                            listener.contentChanged(output);
                                        }
                                    }
                                } catch (Exception ex) {
                                    SoapUI.logError(ex, "Error processing state change to " + newState);
                                }
                            } else if (newState == Worker.State.FAILED && !showingErrorPage) {
                                try {
                                    showingErrorPage = true;
                                    setContent(errorPage == null ? DEFAULT_ERROR_PAGE : errorPage);
                                } finally {
                                    showingErrorPage = false;
                                }
                            }
                        }
                    }
            );
        }

    }
}
