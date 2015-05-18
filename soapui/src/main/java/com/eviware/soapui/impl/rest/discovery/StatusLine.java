package com.eviware.soapui.impl.rest.discovery;

/**
 * @author joel.jonsson
 */
public class StatusLine {

    private String protocolVersion;

    private int statusCode;

    private String reasonPhrase;

    public StatusLine(String protocolVersion, int statusCode, String reasonPhrase) {
        this.protocolVersion = protocolVersion;
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StatusLine that = (StatusLine) o;

        if (statusCode != that.statusCode) {
            return false;
        }
        if (protocolVersion != null ? !protocolVersion.equals(that.protocolVersion) : that.protocolVersion != null) {
            return false;
        }
        if (reasonPhrase != null ? !reasonPhrase.equals(that.reasonPhrase) : that.reasonPhrase != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = protocolVersion != null ? protocolVersion.hashCode() : 0;
        result = 31 * result + statusCode;
        result = 31 * result + (reasonPhrase != null ? reasonPhrase.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", protocolVersion, statusCode, reasonPhrase);
    }

    public static StatusLine of(org.apache.http.StatusLine statusLine) {

        return statusLine == null ? null :
                new StatusLine(
                        statusLine.getProtocolVersion().toString(),
                        statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
    }
}
