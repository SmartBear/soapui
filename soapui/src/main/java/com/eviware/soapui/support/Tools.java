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

package com.eviware.soapui.support;

import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;
import com.eviware.soapui.support.types.StringToStringMap;
import junit.framework.ComparisonFailure;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {
    public static final int COPY_BUFFER_SIZE = 1000;

    private static final MessageSupport messages = MessageSupport.getMessages(Tools.class);
    private static final Logger log = LogManager.getLogger(Tools.class);

    private static final Pattern PROPERTY_EXPANSION_EQUALS_PATTERN = Pattern.compile("^\\$\\{(.*)\\}$");
    private static final Pattern PROPERTY_EXPANSION_CONTAINS_PATTERN =
            Pattern.compile("(\\$\\{(.*?)\\})|(%24%7B.*?%7D)|(%2524%257B.*?%257D)|(%252524%25257B.*?%25257D)");

    public static String[] tokenizeArgs(String args) {
        if (args == null || args.trim().length() == 0) {
            return null;
        }

        List<String> l = Arrays.asList(args.split(" "));
        List<String> result = new ArrayList<String>();

        for (int c = 0; c < l.size(); c++) {
            String s = l.get(c);
            if (s.startsWith("\"")) {
                c++;
                s += " " + l.get(c);
                while (!(s.endsWith("\"") && !s.endsWith("\\\"")) && c < l.size()) {
                    c++;
                }

                // remove trailing/leading quotes
                s = c == l.size() ? s.substring(1) : s.substring(1, s.length() - 1);

                // replace backslashed quotes
                s = s.replace("\\\"", "\"");
            }

            result.add(s);
        }

        return result.toArray(new String[result.size()]);
    }

    public static String convertToHtml(String str) {
        StringBuilder result = new StringBuilder("<html><body>");

        for (int c = 0; c < str.length(); c++) {
            char ch = str.charAt(c);
            if (ch == '\n') {
                result.append("<br>");
            } else {
                result.append(ch);
            }
        }

        result.append("</body></html>");
        return result.toString();
    }

    public static String getFilename(String filePath) {
        if (filePath == null || filePath.length() == 0) {
            return filePath;
        }

        int ix = filePath.lastIndexOf(File.separatorChar);
        if (ix <= 0) {
            return filePath;
        }

        return filePath.substring(ix + 1, filePath.length());
    }

    public static String getDir(String filePath) {
        if (filePath == null || filePath.length() == 0) {
            return filePath;
        }

        int ix = filePath.lastIndexOf(File.separatorChar);
        if (ix <= 0) {
            return filePath;
        }

        return ensureDir(filePath.substring(0, ix), "");
    }

    public static String ensureDir(String dir, String basedir) {
        if (dir == null || dir.length() == 0) {
            return "";
        }

        File dirFile = new File(dir);
        if (!dirFile.isAbsolute()) {
            if (basedir.length() == 0) {
                basedir = new File("").getAbsolutePath();
            }

            dirFile = new File(basedir, dir);
        }

        dirFile.mkdirs();
        return dirFile.getAbsolutePath();
    }

    public static String ensureFileDir(String file, String basedir) {
        if (file == null || file.length() == 0) {
            return "";
        }

        File dirFile = new File(basedir, file);
        if (!dirFile.isAbsolute()) {
            if (basedir.length() == 0) {
                basedir = new File("").getAbsolutePath();
            }

            dirFile = new File(basedir, file);
        }

        String absolutePath = dirFile.getAbsolutePath();
        if (!dirFile.exists()) {
            int ix = absolutePath.lastIndexOf(File.separatorChar);
            File fileDir = new File(absolutePath.substring(0, ix));
            fileDir.mkdirs();
        }

        return absolutePath;
    }

    public static String ensureDir(String outputDir) {
        if (outputDir == null) {
            outputDir = "";
        }

        File output = new File(outputDir);
        output.mkdirs();
        return outputDir;
    }

    // preallocate so it does not consume memory after out-of-memory errors
    private static final byte[] copyBuffer = new byte[8192];
    public static final long READ_ALL = 0;


    public static String modifyUrl(final String url, Integer mods) {

        String helpUrl = url;

        // Integer, since switch strings won't work yet.

        int modifier = 0; // String modifier = "prod";

        if (helpUrl == null) {
            modifier = 1; // "missing";
        } else if (url.substring(0, 4).equals("http")) {
            modifier = 2; // "external";
        } else if (((mods & ActionEvent.SHIFT_MASK) != 0)
                && ((mods & ActionEvent.CTRL_MASK) != 0)) {
            modifier = 3; // "dev";
        } else if (((mods & ActionEvent.SHIFT_MASK) != 0)
                && ((mods & ActionEvent.ALT_MASK) != 0)) {
            modifier = 4; // "next";
        } else {
            modifier = 0; // String modifier = "prod";
        }

        switch (modifier) {
            case 1: // "missing":
                UISupport.showErrorMessage("Missing help URL");
                helpUrl = HelpUrls.MISSING_URL + url;
                break;
            case 2: // "external":
                helpUrl = url;
                break;
            case 3: // "dev":
                helpUrl = HelpUrls.BASE_URL_DEV + url;
                break;
            case 4: // "next":
                helpUrl = HelpUrls.BASE_URL_NEXT + url;
                break;
            default:
                helpUrl = HelpUrls.BASE_URL_PROD + url;
                break;
        }

        return helpUrl;
    }


    public static void openURL(String url) {
        String osName = System.getProperty("os.name");

        try {
            if (osName.startsWith("Mac OS")) {
                Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
                openURL.invoke(null, url);
            } else if (osName.startsWith("Windows")) {
                if (url.startsWith("file:")) {
                    url = URLDecoder.decode(url.substring(5), "utf-8");
                    while (url.startsWith("/")) {
                        url = url.substring(1);
                    }

                    url = url.replace('/', '\\');

                    if (!new File(url).exists()) {
                        UISupport.showErrorMessage("File [" + url + "] not found");
                        return;
                    }
                }

                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else { // assume Unix or Linux
                String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++) {
                    if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                }
                if (browser == null) {
                    throw new Exception("Could not find web browser");
                } else {
                    Runtime.getRuntime().exec(new String[]{browser, url});
                }
            }
        } catch (Exception e) {
            UISupport.showErrorMessage(e);
        }
    }

    public static ByteArrayOutputStream readAll(InputStream instream, long maxSize) throws IOException {
        ByteArrayOutputStream outstream = new ByteArrayOutputStream(4096);

        readAndWrite(instream, maxSize, outstream);

        outstream.close();
        return outstream;
    }

    public static void readAndWrite(InputStream instream, long maxSize, OutputStream outstream) throws IOException {
        byte[] buffer = new byte[4096];
        int len;
        int read = 0;
        int toRead = 4096;

        if (maxSize > 0) {
            if (read + toRead > maxSize) {
                toRead = (int) (maxSize - read);
            }
        }

        while ((len = instream.read(buffer, 0, toRead)) > 0) {
            outstream.write(buffer, 0, len);
            read += toRead;

            if (maxSize > 0) {
                if (read + toRead > maxSize) {
                    toRead = (int) (maxSize - read);
                }
            }
        }
    }

    public static int copyFile(File source, File target, boolean overwrite) throws IOException {
        int bytes = 0;

        if (target.exists()) {
            if (overwrite) {
                target.delete();
            } else {
                return -1;
            }
        } else {
            String path = target.getAbsolutePath();
            int ix = path.lastIndexOf(File.separatorChar);
            if (ix != -1) {
                path = path.substring(0, ix);
                File pathFile = new File(path);
                if (!pathFile.exists()) {
                    pathFile.mkdirs();
                }
            }
        }

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(source));
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target));

        int read = in.read(copyBuffer);
        while (read != -1) {
            if (read > 0) {
                out.write(copyBuffer, 0, read);
                bytes += read;
            }
            read = in.read(copyBuffer);
        }

        in.close();
        out.close();

        return bytes;
    }

    /**
     * Joins a relative url to a base url.. needs improvements..
     */

    public static String joinRelativeUrl(String baseUrl, String url) {
        if (baseUrl.indexOf('?') > 0) {
            baseUrl = baseUrl.substring(0, baseUrl.indexOf('?'));
        }

        boolean isWindowsUrl = baseUrl.indexOf('\\') >= 0;
        boolean isUsedInUnix = File.separatorChar == '/';

        if (isUsedInUnix && isWindowsUrl) {
            baseUrl = baseUrl.replace('\\', '/');
            url = url.replace('\\', '/');
        }

        boolean isFile = baseUrl.startsWith("file:");

        int ix = baseUrl.lastIndexOf('\\');
        if (ix == -1) {
            ix = baseUrl.lastIndexOf('/');
        }

        // absolute?
        if (url.startsWith("/") && !isFile) {
            ix = baseUrl.indexOf("/", baseUrl.indexOf("//") + 2);
            return baseUrl.substring(0, ix) + url;
        }

        // remove leading "./"
        while (url.startsWith(".\\") || url.startsWith("./")) {
            url = url.substring(2);
        }

        // remove leading "../"
        while (url.startsWith("../") || url.startsWith("..\\")) {
            int ix2 = baseUrl.lastIndexOf('\\', ix - 1);
            if (ix2 == -1) {
                ix2 = baseUrl.lastIndexOf('/', ix - 1);
            }
            if (ix2 == -1) {
                break;
            }

            baseUrl = baseUrl.substring(0, ix2 + 1);
            ix = ix2;

            url = url.substring(3);
        }

        // remove "/./"
        while (url.contains("/./") || url.contains("\\.\\")) {
            int ix2 = url.indexOf("/./");
            if (ix2 == -1) {
                ix2 = url.indexOf("\\.\\");
            }

            url = url.substring(0, ix2) + url.substring(ix2 + 2);
        }

        // remove "/../"
        while (url.contains("/../") || url.contains("\\..\\")) {
            int ix2 = -1;

            int ix3 = url.indexOf("/../");
            if (ix3 == -1) {
                ix3 = url.indexOf("\\..\\");
                ix2 = url.lastIndexOf('\\', ix3 - 1);
            } else {
                ix2 = url.lastIndexOf('/', ix3 - 1);
            }

            if (ix2 == -1) {
                break;
            }

            url = url.substring(0, ix2) + url.substring(ix3 + 3);
        }

        String result = baseUrl.substring(0, ix + 1) + url;
        if (isFile) {
            result = result.replace('/', File.separatorChar);
        }

        return result;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static long writeAll(OutputStream out, InputStream in) throws IOException {
        byte[] buffer = new byte[COPY_BUFFER_SIZE];
        long total = 0;

        int sz = in.read(buffer);
        while (sz != -1) {
            out.write(buffer, 0, sz);
            total += sz;
            sz = in.read(buffer);
        }

        return total;
    }

    public static String expandProperties(StringToStringMap values, String content, boolean leaveMissing) {
        int ix = content.indexOf("${");
        if (ix == -1) {
            return content;
        }

        StringBuilder buf = new StringBuilder();
        int lastIx = 0;
        while (ix != -1) {
            buf.append(content.substring(lastIx, ix));

            int ix2 = content.indexOf('}', ix + 2);
            if (ix2 == -1) {
                break;
            }

            int ix3 = content.lastIndexOf("${", ix2);
            if (ix3 != ix) {
                buf.append(content.substring(ix, ix3));
                ix = ix3;
            }

            String propertyName = content.substring(ix + 2, ix2);
            Object property = values.get(propertyName);
            if (property != null) {
                buf.append(property.toString());
            } else if (leaveMissing) {
                buf.append("${").append(propertyName).append('}');
            }

            lastIx = ix2 + 1;
            ix = content.indexOf("${", lastIx);
        }

        if (lastIx < content.length()) {
            buf.append(content.substring(lastIx));
        }

        return buf.toString();
    }

    /**
     * Replaces the host part of the specified endpoint with the specified host
     *
     * @param endpoint the endpoint to modify
     * @param host     the host to set
     * @return the modified endpoint
     */

    public static String replaceHost(String endpoint, String host) {
        int ix1 = endpoint.indexOf("://");
        if (ix1 < 0) {
            return endpoint;
        }

        int ix2 = endpoint.indexOf(":", ix1 + 3);
        if (ix2 == -1 || host.indexOf(":") > 0) {
            ix2 = endpoint.indexOf("/", ix1 + 3);
            if (ix2 == ix1 + 3) {
                ix2 = -1;
            }
        }

        return endpoint.substring(0, ix1) + "://" + host + (ix2 == -1 ? "" : endpoint.substring(ix2));
    }

    public static String getEndpointFromUrl(URL baseUrl) {
        StringBuilder result = new StringBuilder();
        result.append(baseUrl.getProtocol()).append("://");
        result.append(baseUrl.getHost());
        if (baseUrl.getPort() > 0) {
            result.append(':').append(baseUrl.getPort());
        }

        return result.toString();
    }

    public static String getContentTypeFromFilename(String fileName) {
        return ContentTypeHandler.getContentTypeFromFilename(fileName);
    }

    public static String getExtensionForContentType(String contentType) {
        return ContentTypeHandler.getExtensionForContentType(contentType);
    }

    public static String getFileContent(String path) {
        String content = "";
        try {
            FileInputStream fstream = new FileInputStream(path);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            content = br.readLine();
            br.close();
            in.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        return content;
    }

    public static String getTitleProperty(String path) {
        String content = "";
        try {
            FileInputStream fstream = new FileInputStream(path);
            Properties props = new Properties();
            props.load(fstream);
            fstream.close();
            content = props.getProperty("soapui.app.title");
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        return content;
    }

    public static String normalizeFileSeparators(String input) {
        if (input != null && input.startsWith("file:")) {
            input = input.replace('/', File.separatorChar);
            input = input.replace('\\', File.separatorChar);
        }
        return input;
    }

    /**
     * Compares two string for similarity, allows wildcard.
     *
     * @param expected
     * @param real
     * @param wildcard
     * @throws ComparisonFailure
     */
    public static void assertSimilar(String expected, String real, char wildcard) throws ComparisonFailure {
        if (!isSimilar(expected, real, wildcard)) {
            throw new ComparisonFailure("Not matched", expected, real);
        }
    }

    public static boolean isSimilar(String expected, String real, char wildcard) throws ComparisonFailure {

        // expected == wildcard matches all
        if (!expected.equals(String.valueOf(wildcard))) {

            StringBuilder sb = new StringBuilder();
            if (expected.startsWith(String.valueOf(wildcard))) {
                sb.append(".*");
            }
            boolean first = true;
            for (String token : expected.split(Pattern.quote(String.valueOf(wildcard)))) {
                if (token.isEmpty()) {
                    continue;
                }
                if (!first) {
                    sb.append(".*");
                }
                first = false;
                sb.append(Pattern.quote(token));
            }
            if (expected.endsWith(String.valueOf(wildcard))) {
                sb.append(".*");
            }
            if (!Pattern.compile(sb.toString(), Pattern.DOTALL).matcher(real).matches()) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("os.name"));
    }

    public static File createTemporaryDirectory() throws IOException {
        String libDirectoryName = UUID.randomUUID().toString();
        final File libDirectory = new File(System.getProperty("java.io.tmpdir"), libDirectoryName);
        if (!libDirectory.mkdir()) {
            throw new IOException("Could not create directory for unpacked JAR libraries at " + libDirectory);
        }
        deleteDirectoryOnExit(libDirectory);
        return libDirectory;
    }

    public static void deleteDirectoryOnExit(final File directory) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileUtils.deleteDirectory(directory);
                } catch (IOException e) {
                    log.warn("Could not delete temporary directory " + directory);
                }
            }
        }));
    }

    public static boolean isPropertyExpansion(@Nullable String value) {
        return value != null && PROPERTY_EXPANSION_EQUALS_PATTERN.matcher(value).matches();
    }

    public static String removePropertyExpansions(String definitionUrl, String definition) {
        Matcher matcher = PROPERTY_EXPANSION_CONTAINS_PATTERN.matcher(definition);
        while (matcher.find()) {
            log.warn(messages.get("Tools.Warning.PropertyExpansionRemovedFromDefinition",
                    definitionUrl, matcher.group()));
        }
        return matcher.replaceAll("");
    }
}
