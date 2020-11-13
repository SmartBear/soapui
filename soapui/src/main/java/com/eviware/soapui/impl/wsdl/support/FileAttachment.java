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

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.teststeps.BeanPathPropertySupport;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;
import com.eviware.soapui.support.resolver.ResolveContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Attachments cached locally for each request
 *
 * @author Ole.Matzura
 */

public abstract class FileAttachment<T extends AbstractWsdlModelItem<?>> implements WsdlAttachment {
    private AttachmentConfig config;
    private final static Logger log = LogManager.getLogger(FileAttachment.class);
    private final T modelItem;
    private BeanPathPropertySupport urlProperty;

    public FileAttachment(T modelItem, AttachmentConfig config) {
        this.modelItem = modelItem;
        this.config = config;

        if (config.getTempFilename() != null) {
            try {
                log.info("Moving locally cached file [" + config.getTempFilename() + "] to internal cache..");
                File tempFile = new File(config.getTempFilename());
                cacheFileLocally(tempFile);
            } catch (IOException e) {
                if (!config.isSetData()) {
                    config.setData(new byte[0]);
                    config.setSize(0);
                }

                SoapUI.logError(e);
            }
        }

        if (isCached()) {
            if (config.isSetTempFilename()) {
                config.unsetTempFilename();
            }

            if (config.isSetUrl()) {
                config.unsetUrl();
            }
        }

        urlProperty = new BeanPathPropertySupport(modelItem, config, "url");
    }

    public FileAttachment(T modelItem, File file, boolean cache, AttachmentConfig config) throws IOException {
        this(modelItem, config);

        config.setName(file.getName());
        config.setContentType(ContentTypeHandler.getContentTypeFromFilename(file.getName()));
        config.setContentId(file.getName());
        config.setId(UUID.randomUUID().toString());

        // cache locally if specified
        if (cache) {
            cacheFileLocally(file);
        }

        urlProperty.set(file.getPath(), false);
    }

    public void setName(String value) {
        config.setName(value);
    }

    public void setUrl(String url) {
        urlProperty.set(url, true);
    }

    public void reload(File file, boolean cache) throws IOException {
        config.setName(file.getName());
        config.setContentType(ContentTypeHandler.getContentTypeFromFilename(file.getName()));
        config.setContentId(file.getName());

        // cache locally if specified
        if (cache) {
            cacheFileLocally(file);
        } else {
            urlProperty.set(file.getPath(), false);
            config.unsetData();
        }
    }

    public T getModelItem() {
        return modelItem;
    }

    public void cacheFileLocally(File file) throws FileNotFoundException, IOException {
        // write attachment-data to tempfile
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(data);
        out.putNextEntry(new ZipEntry(config.getName()));

        InputStream in = new FileInputStream(file);
        long sz = file.length();
        config.setSize(sz);

        Tools.writeAll(out, in);

        in.close();
        out.closeEntry();
        out.finish();
        out.close();
        data.close();

        config.setData(data.toByteArray());
    }

    public String getContentType() {
        AttachmentEncoding encoding = getEncoding();
        if (encoding == AttachmentEncoding.NONE) {
            return config.getContentType();
        } else {
            return "application/octet-stream";
        }
    }

    public InputStream getInputStream() throws IOException {
        BufferedInputStream inputStream = null;

        if (isCached()) {
            ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(config.getData()));
            zipInputStream.getNextEntry();
            inputStream = new BufferedInputStream(zipInputStream);
        } else {
            String url = urlProperty.expand();
            inputStream = new BufferedInputStream(url == null ? new ByteArrayInputStream(new byte[0])
                    : new FileInputStream(url));
        }

        AttachmentEncoding encoding = getEncoding();
        if (encoding == AttachmentEncoding.BASE64) {
            ByteArrayOutputStream data = Tools.readAll(inputStream, Tools.READ_ALL);
            return new ByteArrayInputStream(Base64.encodeBase64(data.toByteArray()));
        } else if (encoding == AttachmentEncoding.HEX) {
            ByteArrayOutputStream data = Tools.readAll(inputStream, Tools.READ_ALL);
            return new ByteArrayInputStream(new String(Hex.encodeHex(data.toByteArray())).getBytes());
        }

        return inputStream;
    }

    public String getName() {
        return config.getName();
    }

    public long getSize() {
        if (isCached()) {
            return config.getSize();
        } else {
            String url = urlProperty.expand();
            if (url != null) {
                File file = new File(url);
                if (file.exists()) {
                    return file.length();
                }
            }
        }

        return -1;
    }

    public void release() {
        if (isCached()) {
            new File(config.getTempFilename()).delete();
        }
    }

    public String getPart() {
        return config.getPart();
    }

    public void setContentType(String contentType) {
        config.setContentType(contentType);
    }

    public void setPart(String part) {
        config.setPart(part);
    }

    public void setData(byte[] data) {
        try {
            // write attachment-data to tempfile
            ByteArrayOutputStream tempData = new ByteArrayOutputStream();
            ZipOutputStream out = new ZipOutputStream(tempData);
            out.putNextEntry(new ZipEntry(config.getName()));
            config.setSize(data.length);
            out.write(data);
            out.closeEntry();
            out.finish();
            out.close();
            config.setData(tempData.toByteArray());
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    public byte[] getData() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Tools.writeAll(out, getInputStream());
        return out.toByteArray();
    }

    public String getUrl() {
        return urlProperty.get();
    }

    public boolean isCached() {
        return config.isSetData();
    }

    abstract public AttachmentType getAttachmentType();

    public void updateConfig(AttachmentConfig config) {
        this.config = config;
        urlProperty.setConfig(config);
    }

    public AttachmentConfig getConfig() {
        return config;
    }

    public void setContentID(String contentID) {
        if ((contentID == null || contentID.length() == 0) && config.isSetContentId()) {
            config.unsetContentId();
        } else {
            config.setContentId(contentID);
        }
    }

    public String getContentID() {
        return config.getContentId();
    }

    public void resolve(ResolveContext<?> context) {
        if (!isCached()) {
            urlProperty.resolveFile(context, "Missing attachment [" + getName() + "]", null, null, false);
        }
    }

    public String getContentEncoding() {
        AttachmentEncoding encoding = getEncoding();
        if (encoding == AttachmentEncoding.BASE64) {
            return "base64";
        } else if (encoding == AttachmentEncoding.HEX) {
            return "hex";
        } else {
            return "binary";
        }
    }

    public void addExternalDependency(List<ExternalDependency> dependencies) {
        if (!isCached()) {
            dependencies.add(new PathPropertyExternalDependency(urlProperty));
        }
    }
}
