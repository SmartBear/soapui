package com.eviware.soapui.impl.wsdl.support.http;

import java.io.IOException;
import java.io.OutputStream;

public class SoapUILoggingOutputStream extends OutputStream {
    private final OutputStream out;
    private final SoapUIWire wire;

    public SoapUILoggingOutputStream(final OutputStream out, final SoapUIWire wire) {
        super();
        this.out = out;
        this.wire = wire;
    }

    @Override
    public void write(final int b) throws IOException {
        try {
            wire.output(b);
        } catch (final IOException ex) {
            wire.output("[write] I/O error: " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void write(final byte[] b) throws IOException {
        try {
            wire.output(b);
            out.write(b);
        } catch (final IOException ex) {
            wire.output("[write] I/O error: " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        try {
            wire.output(b, off, len);
            out.write(b, off, len);
        } catch (final IOException ex) {
            wire.output("[write] I/O error: " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void flush() throws IOException {
        try {
            out.flush();
        } catch (final IOException ex) {
            wire.output("[flush] I/O error: " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            out.close();
        } catch (final IOException ex) {
            wire.output("[close] I/O error: " + ex.getMessage());
            throw ex;
        }
    }
}
