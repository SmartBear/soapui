package com.eviware.soapui.support;

import java.util.Objects;

public class NodeRange {
    public int startLine;
    public int endLine;

    public NodeRange() {
        this(-1, -1);
    }

    public NodeRange(int startLine, int endLine) {
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public NodeRange(int startLine) {
        this(startLine, -1);
    }

    public String toString() {
        return startLine + "-" + endLine;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof NodeRange)) {
            return false;
        }

        NodeRange nodeRange = (NodeRange) obj;
        return Objects.equals(startLine, nodeRange.startLine) && Objects.equals(endLine, nodeRange.endLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startLine, endLine);
    }
}
