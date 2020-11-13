/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.testondemand;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SoapUISystemProperties;
import com.eviware.soapui.config.TestOnDemandCommandConfig;
import com.eviware.soapui.config.TestOnDemandContentConfig;
import com.eviware.soapui.config.TestOnDemandFileConfig;
import com.eviware.soapui.config.TestOnDemandHeaderConfig;
import com.eviware.soapui.config.TestOnDemandKeystoreConfig;
import com.eviware.soapui.config.TestOnDemandKeystorePasswordConfig;
import com.eviware.soapui.config.TestOnDemandLocationsRequestDocumentConfig;
import com.eviware.soapui.config.TestOnDemandLocationsRequestDocumentConfig.TestOnDemandLocationsRequest;
import com.eviware.soapui.config.TestOnDemandLocationsRequestDocumentConfig.TestOnDemandLocationsRequest.Request;
import com.eviware.soapui.config.TestOnDemandProjectPasswordConfig;
import com.eviware.soapui.config.TestOnDemandTestCaseConfig;
import com.eviware.soapui.config.TestOnDemandTestSuiteConfig;
import com.eviware.soapui.config.TestOnDemandTxnConfig;
import com.eviware.soapui.config.TestOnDemandUploadBodyConfig;
import com.eviware.soapui.config.TestOnDemandUploadRequestDocumentConfig;
import com.eviware.soapui.config.TestOnDemandUploadRequestDocumentConfig.TestOnDemandUploadRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.settings.SSLSettings;
import com.eviware.soapui.support.xml.XmlUtils;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Erik R. Yverling
 *         <p/>
 *         Calls the AlertSite API for running Test On Demand.
 */

public class TestOnDemandCaller {
    // FIXME Should these be in a configuration file instead?

    private final static String DEFAULT_PROTOCOL = "https";
    private final static String PROTOCOL_DELIMITER = "://";
    private final static String PROD_HOST = "www.alertsite.com";
    private static final String LOCATIONS_PATH = "/restapi/v2/devices/list/locations";
    private static final String UPLOAD_PATH = "/restapi/v2/devices/upload/testondemand";
    private final static String TEST_ON_DEMAND_DOMAIN = getDomain();
    private static final String LOCATIONS_URI = getProtocol() + PROTOCOL_DELIMITER + TEST_ON_DEMAND_DOMAIN
            + LOCATIONS_PATH;
    private static final String UPLOAD_URI = getProtocol() + PROTOCOL_DELIMITER + TEST_ON_DEMAND_DOMAIN + UPLOAD_PATH;

    private static final String REDIRECT_URL_XPATH_EXPRESSION = "//RedirectURL";
    private static final String LOCATION_XPATH_EXPRESSION = "//Location";
    private static final String LOCATION_CODE_XPATH_EXPRESSION = "LocCode";
    private static final String LOCATION_NAME_XPATH_EXPRESSION = "LocName";
    private static final String LOCATION_SERVER_IP_ADDRESSES_XPATH_EXPRESSION = "LocIPs";

    private static final String API_VERSION = "2";
    private static final String APPLICATION_ZIP = "application/zip";
    private static final String BASE64 = "base64";
    private static final String USER_AGENT = "soapUI-" + SoapUI.SOAPUI_VERSION;

    private static final String LOCATIONS_NAME = "ListLocations";
    private static final String LOCATIONS_PARAMETER = "server_attrib=ITEST";

    private static final String UPLOAD_NAME = "TestOnDemand";
    private static final String UPLOAD_PARAMETER_LOCATION_PREFIX = "test_location=";

    private static final String SERVER_IP_ADDRESSES_DELIMETER = ",";

    protected static final String COULD_NOT_SAVE_TEMPORARY_PROJECT_MESSAGE = "Could not save temporary project file before sending TestCase";

    private final XPath xpath = XPathFactory.newInstance().newXPath();

    private static final Logger log = LogManager.getLogger(TestOnDemandCaller.class);

    @Nonnull
    public List<Location> getLocations() throws Exception {
        Document responseDocument = makeCall(LOCATIONS_URI, generateLocationsRequestXML());
        NodeList locationNodes = (NodeList) xpath.evaluate(LOCATION_XPATH_EXPRESSION, responseDocument,
                XPathConstants.NODESET);

        List<Location> locations = new ArrayList<Location>();
        for (int i = 0; i < locationNodes.getLength(); i++) {
            Node locationNode = locationNodes.item(i);
            String name = (String) xpath.evaluate(LOCATION_NAME_XPATH_EXPRESSION, locationNode, XPathConstants.STRING);
            String code = (String) xpath.evaluate(LOCATION_CODE_XPATH_EXPRESSION, locationNode, XPathConstants.STRING);
            String unformattedServerIPAddresses = (String) xpath.evaluate(LOCATION_SERVER_IP_ADDRESSES_XPATH_EXPRESSION,
                    locationNode, XPathConstants.STRING);

            String[] serverIPAddresses = new String[0];
            if (!unformattedServerIPAddresses.isEmpty()) {
                serverIPAddresses = unformattedServerIPAddresses.split(SERVER_IP_ADDRESSES_DELIMETER);
            }

            locations.add(new Location(code, name, serverIPAddresses));
        }

        return locations;
    }

    // FIXME We should do some performance testing of large soapUI project files.
    @Nonnull
    public String sendTestCase(@Nonnull WsdlTestCase testCase, @Nonnull Location location) throws Exception {

        final ExtendedPostMethod post = new ExtendedPostMethod();
        post.setURI(new URI(UPLOAD_URI));

        String locationCode = location.getCode();

        String encodedTestSuiteName = getBase64EncodedString(testCase.getTestSuite().getName().getBytes());
        String encodedTestCaseName = getBase64EncodedString(testCase.getName().getBytes());

        File tempProjectFile = saveTemporaryProject(testCase.getTestSuite().getProject());
        byte[] projectFileData = getBytes(tempProjectFile.getAbsolutePath());
        byte[] zipedProjectFileData = zipBytes(testCase.getTestSuite().getProject().getName(), projectFileData);
        String encodedZipedProjectFile = getBase64EncodedString(zipedProjectFileData);

        String projectPassword = testCase.getTestSuite().getProject().getShadowPassword();
        String encodedProjectPassword = getBase64EncodedString(Strings.nullToEmpty(projectPassword).getBytes());

        String keystoreFilePath = SoapUI.getSettings().getString(SSLSettings.KEYSTORE, "");
        byte[] keystoreFileData = getBytes(keystoreFilePath);
        String encodedKeystoreFile = getBase64EncodedString(keystoreFileData);

        String encodedKeystorePassword = getBase64EncodedString(SoapUI.getSettings()
                .getString(SSLSettings.KEYSTORE_PASSWORD, "").getBytes());

        String requestContent = generateUploadRequestXML(locationCode, encodedTestSuiteName, encodedTestCaseName,
                encodedZipedProjectFile, encodedProjectPassword, encodedKeystoreFile, encodedKeystorePassword);

        byte[] compressedRequestContent = CompressionSupport.compress(CompressionSupport.ALG_GZIP,
                requestContent.getBytes());
        post.setEntity(new ByteArrayEntity(compressedRequestContent));

        Document responseDocument = makeCall(UPLOAD_URI, requestContent);
        String redirectURL = (String) xpath.evaluate(REDIRECT_URL_XPATH_EXPRESSION, responseDocument,
                XPathConstants.STRING);

        if (Strings.isNullOrEmpty(redirectURL)) {
            throw new RuntimeException("The RedirectURL element is missing in the response message");
        }
        return redirectURL;
    }

    // FIXME Add to utility class and make DependencyValidator.saveProject() use this
    protected File saveTemporaryProject(WsdlProject project) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("project-temp-", ".xml", null);
            project.saveIn(tempFile);
        } catch (IOException e) {
            SoapUI.logError(e, COULD_NOT_SAVE_TEMPORARY_PROJECT_MESSAGE);
        }
        return tempFile;
    }

    private Document makeCall(String uri, String requestContent) throws Exception {
        final ExtendedPostMethod post = new ExtendedPostMethod();
        post.setURI(new URI(uri));

        post.setEntity(new StringEntity(requestContent));

        // FIXME Should we remove the logging printouts before release? The upload request maybe would be to large?

        log.debug("Sending request to " + uri);
        log.debug(requestContent);

        HttpClientSupport.execute(post);

        byte[] responseBody = post.getResponseBody();

        log.debug("Got response from " + uri);
        log.debug(new String(responseBody));

        String reponseBodyAsString = new String(responseBody);
        return XmlUtils.parseXml(reponseBodyAsString);
    }

    private String generateLocationsRequestXML() {
        TestOnDemandLocationsRequest locationsRequest = TestOnDemandLocationsRequestDocumentConfig.Factory.newInstance()
                .addNewTestOnDemandLocationsRequest();

        Request request = locationsRequest.addNewRequest();
        request.setApiVersion(API_VERSION);

        TestOnDemandHeaderConfig header = request.addNewHeader();
        header.setUserAgent(USER_AGENT);

        TestOnDemandCommandConfig command = request.addNewBody().addNewCommand();
        command.setName(LOCATIONS_NAME);
        command.setParameters(LOCATIONS_PARAMETER);
        return locationsRequest.xmlText(getXmlOptionsWithoutNamespaces());
    }

    private String generateUploadRequestXML(String locationCode, String encodedTestSuiteName,
                                            String encodedTestCaseName, String encodedZipedProjectFile, String encodedProjectPassword,
                                            String encodedKeystoreFile, String encodedKeystorePassword) {
        TestOnDemandUploadRequest uploadRequestConfig = TestOnDemandUploadRequestDocumentConfig.Factory.newInstance()
                .addNewTestOnDemandUploadRequest();

        com.eviware.soapui.config.TestOnDemandUploadRequestDocumentConfig.TestOnDemandUploadRequest.Request requestConfig = uploadRequestConfig
                .addNewRequest();
        requestConfig.setApiVersion(API_VERSION);

        TestOnDemandHeaderConfig headerConfig = requestConfig.addNewHeader();
        headerConfig.setUserAgent(USER_AGENT);

        TestOnDemandUploadBodyConfig bodyConfig = requestConfig.addNewBody();

        TestOnDemandCommandConfig commandConfig = bodyConfig.addNewCommand();
        commandConfig.setName(UPLOAD_NAME);
        commandConfig.setParameters(UPLOAD_PARAMETER_LOCATION_PREFIX + locationCode);

        TestOnDemandTxnConfig txnConfig = bodyConfig.addNewTxn();

        TestOnDemandTestSuiteConfig testSuiteConfig = txnConfig.addNewTestSuite();
        testSuiteConfig.setEnctype(BASE64);
        testSuiteConfig.setStringValue(encodedTestSuiteName);

        TestOnDemandTestCaseConfig testCaseConfig = txnConfig.addNewTestCase();
        testCaseConfig.setEnctype(BASE64);
        testCaseConfig.setStringValue(encodedTestCaseName);

        TestOnDemandContentConfig contentConfig = txnConfig.addNewContent();
        contentConfig.setEnctype(BASE64);
        contentConfig.setType(APPLICATION_ZIP);
        contentConfig.setStringValue(encodedZipedProjectFile);

        TestOnDemandProjectPasswordConfig projectPasswordConfig = txnConfig.addNewPassword();
        projectPasswordConfig.setEnctype(BASE64);
        projectPasswordConfig.setStringValue(encodedProjectPassword);

        TestOnDemandKeystoreConfig keystoreConfig = bodyConfig.addNewKeystore();

        TestOnDemandFileConfig fileConfig = keystoreConfig.addNewFile();
        fileConfig.setEnctype(BASE64);
        fileConfig.setStringValue(encodedKeystoreFile);

        TestOnDemandKeystorePasswordConfig keystorePasswordConfig = keystoreConfig.addNewPassword();
        keystorePasswordConfig.setEnctype(BASE64);
        keystorePasswordConfig.setStringValue(encodedKeystorePassword);

        return uploadRequestConfig.xmlText(getXmlOptionsWithoutNamespaces());
    }

    private XmlOptions getXmlOptionsWithoutNamespaces() {
        XmlOptions options = new XmlOptions();
        options.setUseDefaultNamespace();
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("", "http://eviware.com/soapui/config");
        options.setSaveImplicitNamespaces(namespaces);
        return options;
    }

    private static byte[] getBytes(String filePath) throws IOException {
        byte[] byteArray = new byte[0];
        if (!Strings.isNullOrEmpty(filePath)) {
            File file = new File(filePath);
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                byteArray = ByteStreams.toByteArray(inputStream);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
        return byteArray;
    }

    private static byte[] zipBytes(String filename, byte[] dataToBeZiped) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zipedOutputStream = new ZipOutputStream(outputStream);
        ZipEntry entry = new ZipEntry(filename);
        entry.setSize(dataToBeZiped.length);
        try {
            zipedOutputStream.putNextEntry(entry);
            zipedOutputStream.write(dataToBeZiped);
        } finally {
            zipedOutputStream.closeEntry();
            zipedOutputStream.close();
        }
        return outputStream.toByteArray();
    }

    private static String getDomain() {
        String customEndpoint = System.getProperty(SoapUISystemProperties.TEST_ON_DEMAND_HOST);

        if (customEndpoint == null) {
            return PROD_HOST;
        } else {
            return customEndpoint;
        }
    }

    private static String getProtocol() {
        return System.getProperty(SoapUISystemProperties.TEST_ON_DEMAND_PROTOCOL, DEFAULT_PROTOCOL);
    }

    private static String getBase64EncodedString(byte[] bytesToEncode) {
        return new String(Base64.encodeBase64(bytesToEncode));
    }
}
