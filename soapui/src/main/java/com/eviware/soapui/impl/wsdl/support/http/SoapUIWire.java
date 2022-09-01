package com.eviware.soapui.impl.wsdl.support.http;

import com.smartbear.soapui.core.Logging;
import org.apache.http.util.Args;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Formats and logs data to the ReadyAPI HTTP LOG.
 * */
public class SoapUIWire {
    private final Logger log;
    private static final Pattern REQUEST_LINE_PATTERN = Pattern.compile("^[POST|GET|PUT|DELETE|PATCH|HEAD|OPTIONS|TRACE].* HTTP\\/1\\.[0|1]");
    private static final Pattern RESPONSE_LINE_PATTERN = Pattern.compile("^HTTP\\/1\\.[0|1] \\d{3} ");

    public SoapUIWire(Logger log) {
        this.log = log;
    }

    private void wire(boolean request, final InputStream instream)
            throws IOException {
        final StringBuilder buffer = new StringBuilder();
        int ch;
        while ((ch = instream.read()) != -1) {
            if (ch == 13) {
                //ignore carriage return
            } else if (ch == 10) {
                //log if line feed
                String line = buffer.toString();
                log.debug(appendMarkerIfNeeded(request, line), line);
                buffer.setLength(0);
            } else if ((ch < 32) || (ch > 127)) {
                // to hex if control code
                buffer.append("[0x");
                buffer.append(Integer.toHexString(ch));
                buffer.append("]");
            } else {
                buffer.append((char) ch);
            }
        }
        if (buffer.length() > 0) {
            String line = buffer.toString();
            log.debug(appendMarkerIfNeeded(request, line), line);
        }
    }


    public boolean enabled() {
        return log.isDebugEnabled();
    }

    public void output(final InputStream outstream)
            throws IOException {
        Args.notNull(outstream, "Output");
        wire(true, outstream);
    }

    public void input(final InputStream instream)
            throws IOException {
        Args.notNull(instream, "Input");
        wire(false, instream);
    }

    public void output(final byte[] b, final int off, final int len)
            throws IOException {
        Args.notNull(b, "Output");
        wire(true, new ByteArrayInputStream(b, off, len));
    }

    public void input(final byte[] b, final int off, final int len)
            throws IOException {
        Args.notNull(b, "Input");
        wire(false, new ByteArrayInputStream(b, off, len));
    }

    public void output(final byte[] b)
            throws IOException {
        Args.notNull(b, "Output");
        wire(true, new ByteArrayInputStream(b));
    }

    public void input(final byte[] b)
            throws IOException {
        Args.notNull(b, "Input");
        wire(false, new ByteArrayInputStream(b));
    }

    public void output(final int b)
            throws IOException {
        output(new byte[]{(byte) b});
    }

    public void input(final int b)
            throws IOException {
        input(new byte[]{(byte) b});
    }

    public void output(final String s)
            throws IOException {
        Args.notNull(s, "Output");
        output(s.getBytes());
    }

    public void input(final String s)
            throws IOException {
        Args.notNull(s, "Input");
        input(s.getBytes());
    }

    private Marker appendMarkerIfNeeded(boolean request, String line) {
        Matcher matcher;
        if (request) {
            matcher = REQUEST_LINE_PATTERN.matcher(line);
            if (matcher.find()) {
                return Logging.HTTP_CLIENT_WIRE_LOG_TIMESTAMP_MARKER_OUTGOING;
            }
        } else {
            matcher = RESPONSE_LINE_PATTERN.matcher(line);
            if (matcher.find()) {
                return Logging.HTTP_CLIENT_WIRE_LOG_TIMESTAMP_MARKER_INCOMING;
            }
        }
        return null;
    }

}
