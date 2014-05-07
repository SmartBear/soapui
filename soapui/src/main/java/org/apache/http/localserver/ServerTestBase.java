/*
 * Copyright 2004-2014 SmartBear Software
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
*//*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.localserver;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.junit.Before;

/**
 * Base class for tests using {@link LocalTestServer LocalTestServer}. Note that
 * the test server will be {@link #setUp set up} before each individual tests
 * and {@link #tearDown teared down} afterwards. Use this base class
 * <i>exclusively</i> for tests that require the server. If you have some tests
 * that require the server and others that don't, split them in two different
 * classes.
 */
public abstract class ServerTestBase extends BasicServerTestBase {

    /**
     * The available schemes.
     */
    protected SchemeRegistry supportedSchemes;

    /**
     * The default parameters for the client side.
     */
    protected HttpParams defaultParams;

    /**
     * The HTTP processor for the client side.
     */
    protected BasicHttpProcessor httpProcessor;

    /**
     * The default context for the client side.
     */
    protected BasicHttpContext httpContext;

    /**
     * The request executor for the client side.
     */
    protected HttpRequestExecutor httpExecutor;

    /**
     * Prepares the local server for testing. Derived classes that override this
     * method MUST call the implementation here. That SHOULD be done at the
     * beginning of the overriding method. <br/>
     * Derived methods can modify for example the default parameters being set
     * up, or the interceptors.
     * <p/>
     * This method will re-use the helper objects from a previous run if they are
     * still available. For example, the local test server will be re-started
     * rather than re-created. {@link #httpContext httpContext} will always be
     * re-created. Tests that modify the other helper objects should afterwards
     * set the respective attributes to <code>null</code> in a
     * <code>finally{}</code> block to force re-creation for subsequent tests. Of
     * course that shouldn't be done with the test server, or only after shutting
     * that down.
     *
     * @throws Exception in case of a problem
     */
    @Before
    public void setUp() throws Exception {

        if (defaultParams == null) {
            defaultParams = new SyncBasicHttpParams();
            HttpProtocolParams.setVersion(defaultParams, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(defaultParams, "UTF-8");
            HttpProtocolParams.setUserAgent(defaultParams, "TestAgent/1.1");
            HttpProtocolParams.setUseExpectContinue(defaultParams, false);
        }

        if (supportedSchemes == null) {
            supportedSchemes = new SchemeRegistry();
            SchemeSocketFactory sf = PlainSocketFactory.getSocketFactory();
            supportedSchemes.register(new Scheme("http", 80, sf));
        }

        if (httpProcessor == null) {
            httpProcessor = new BasicHttpProcessor();
            httpProcessor.addInterceptor(new RequestContent());
            httpProcessor.addInterceptor(new RequestConnControl()); // optional
        }

        // the context is created each time, it may get modified by test cases
        httpContext = new BasicHttpContext(null);

        if (httpExecutor == null) {
            httpExecutor = new HttpRequestExecutor();
        }

        if (localServer == null) {
            localServer = new LocalTestServer(null, null);
            localServer.registerDefaultHandlers();
        }

        localServer.start();

    } // setUp

    /**
     * Opens a connection to the given target using {@link #defaultParams default
     * parameters}. Maps to {@link #connectTo(HttpHost, HttpParams)
     * connectTo(target,defaultParams)}.
     *
     * @param target the target to connect to
     * @return a new connection opened to the target
     * @throws Exception in case of a problem
     */
    protected DefaultHttpClientConnection connectTo(HttpHost target) throws Exception {

        return connectTo(target, defaultParams);
    }

    /**
     * Opens a connection to the given target using the given parameters.
     *
     * @param target the target to connect to
     * @return a new connection opened to the target
     * @throws Exception in case of a problem
     */
    protected DefaultHttpClientConnection connectTo(HttpHost target, HttpParams params) throws Exception {

        Scheme schm = supportedSchemes.get(target.getSchemeName());
        int port = schm.resolvePort(target.getPort());

        DefaultHttpClientConnection conn = new DefaultHttpClientConnection();
        InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(target.getHostName()), port);
        Socket sock = schm.getSchemeSocketFactory().connectSocket(null, address, null, params);
        conn.bind(sock, params);

        return conn;
    }

}
