package com.eviware.soapui.impl.wsdl.submit.transports.http;

import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.config.HttpRequestConfig;
import com.eviware.soapui.impl.support.http.HttpRequest;
import com.eviware.soapui.impl.wsdl.support.RequestFileAttachment;
import com.eviware.soapui.model.iface.Attachment;
import org.htmlunit.util.MimeType;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class HTMLPageSourceDownloaderTest {
    private static final String BASE_URL = "http://example.com";
    private static final String TEST_CONTENT = "test content";
    private static final String SHORT_NAME_TEST_CONTENT = "short name test";
    private static final String NO_EXTENSION_TEST_CONTENT = "no extension test";
    private static final String ROOT_PATH_TEST_CONTENT = "root path test";
    private static final int MOCK_SERVER_PORT = 18888;

    private HTMLPageSourceDownloader downloader;

    @Mock
    private HttpRequest mockRequest;

    @Mock
    private HttpRequestConfig mockConfig;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        downloader = new HTMLPageSourceDownloader();

        when(mockRequest.getConfig()).thenReturn(mockConfig);
        when(mockConfig.addNewAttachment()).thenAnswer(invocation -> AttachmentConfig.Factory.newInstance());
    }

    @Test
    public void createsAttachmentWithNormalFileName() throws IOException {
        byte[] testData = TEST_CONTENT.getBytes();
        URL url = new URL(BASE_URL + "/path/to/image.png");

        Attachment attachment = downloader.createAttachment(testData, url, mockRequest);

        assertThat(attachment).isNotNull()
                .isInstanceOf(RequestFileAttachment.class);

        RequestFileAttachment fileAttachment = (RequestFileAttachment) attachment;
        String name = fileAttachment.getName();

        assertThat(name).contains("image");
        assertThat(name).endsWith(".png");

        File file = new File(fileAttachment.getUrl());
        assertFileContent(file, testData);
    }

    @Test
    public void createsAttachmentWithShortFileName() throws IOException {
        byte[] testData = SHORT_NAME_TEST_CONTENT.getBytes();
        URL url = new URL(BASE_URL + "/ab.js");

        Attachment attachment = downloader.createAttachment(testData, url, mockRequest);

        assertThat(attachment).isNotNull()
                .isInstanceOf(RequestFileAttachment.class);

        RequestFileAttachment fileAttachment = (RequestFileAttachment) attachment;
        String name = fileAttachment.getName();

        assertThat(name).contains("ab___");
        assertThat(name).endsWith(".js");

        File file = new File(fileAttachment.getUrl());
        assertFileContent(file, testData);
    }

    @Test
    public void createsAttachmentWithNoExtension() throws IOException {
        byte[] testData = NO_EXTENSION_TEST_CONTENT.getBytes();
        URL url = new URL(BASE_URL + "/path/filename");

        Attachment attachment = downloader.createAttachment(testData, url, mockRequest);

        assertThat(attachment).isNotNull()
                .isInstanceOf(RequestFileAttachment.class);

        RequestFileAttachment fileAttachment = (RequestFileAttachment) attachment;
        String name = fileAttachment.getName();

        assertThat(name).contains("filename");

        File file = new File(fileAttachment.getUrl());
        assertFileContent(file, testData);
    }

    @Test
    public void createsAttachmentWithEmptyByteArray() throws IOException {
        byte[] testData = new byte[0];
        URL url = new URL(BASE_URL + "/empty.txt");

        Attachment attachment = downloader.createAttachment(testData, url, mockRequest);

        assertThat(attachment).isNotNull()
                .isInstanceOf(RequestFileAttachment.class);

        RequestFileAttachment fileAttachment = (RequestFileAttachment) attachment;
        File file = new File(fileAttachment.getUrl());

        try {
            assertThat(file).exists();
            assertThat(file).hasSize(0);
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    @Test
    public void createsAttachmentWithRootPath() throws IOException {
        byte[] testData = ROOT_PATH_TEST_CONTENT.getBytes();
        URL url = new URL(BASE_URL + "/file.html");

        Attachment attachment = downloader.createAttachment(testData, url, mockRequest);

        assertThat(attachment).isNotNull()
                .isInstanceOf(RequestFileAttachment.class);

        RequestFileAttachment fileAttachment = (RequestFileAttachment) attachment;
        String name = fileAttachment.getName();

        assertThat(name).contains("file");
        assertThat(name).endsWith(".html");

        File file = new File(fileAttachment.getUrl());
        try {
            assertThat(file).exists();
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    @Test
    public void downloadsCssAndImagesWithEmbeddedServer() throws Exception {
        Server server = getServer();
        List<File> filesToCleanup = new ArrayList<>();
        try {
            server.start();
            String endpoint = "http://localhost:" + MOCK_SERVER_PORT + "/";
            List<Attachment> attachments = downloader.downloadCssAndImages(endpoint, mockRequest);

            assertThat(attachments).isNotNull().hasSize(2);

            for (Attachment attachment : attachments) {
                assertThat(attachment).isInstanceOf(RequestFileAttachment.class);
                RequestFileAttachment fileAttachment = (RequestFileAttachment) attachment;
                assertThat(fileAttachment.getName()).isNotNull();

                File file = new File(fileAttachment.getUrl());
                filesToCleanup.add(file);
                assertThat(file).exists();
            }
        } finally {
            server.stop();
            for (File file : filesToCleanup) {
                Files.deleteIfExists(file.toPath());
            }
        }
    }

    private static void assertFileContent(File file, byte[] testData) throws IOException {
        try {
            assertThat(file).exists();
            byte[] readData = Files.readAllBytes(file.toPath());
            assertThat(testData).containsExactly(readData);
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    @NotNull
    private static Server getServer() {
        Server server = new Server(MOCK_SERVER_PORT);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
                    throws IOException {
                response.setContentType(MimeType.TEXT_HTML);
                response.setStatus(HttpServletResponse.SC_OK);

                switch (target) {
                    case "/":
                        String html = "<!DOCTYPE html><html><head>" +
                                "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://localhost:" + MOCK_SERVER_PORT + "/style.css\">" +
                                "</head><body>" +
                                "<img src=\"http://localhost:" + MOCK_SERVER_PORT + "/image.png\" alt=\"Test\">" +
                                "</body></html>";
                        response.getWriter().println(html);
                        break;
                    case "/style.css":
                        response.setContentType(MimeType.TEXT_CSS);
                        response.getWriter().println("body { color: red; }");
                        break;
                    case "/image.png":
                        response.setContentType(MimeType.IMAGE_PNG);
                        response.getOutputStream().write(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}); // Simplest PNG header bytes
                        break;
                    default:
                        response.setContentType(MimeType.TEXT_HTML);
                }

                ((org.mortbay.jetty.Request) request).setHandled(true);
            }
        });
        return server;
    }
}