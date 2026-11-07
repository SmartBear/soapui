package com.eviware.soapui.Util;

import com.eviware.soapui.SoapUI;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ByteEncodingDetector {
    public String detectEncoding(byte[] data) {
        try {
            InputStream stream = new ByteArrayInputStream(data);
            CharsetDetector detector = new CharsetDetector();
            detector.setText(stream);
            CharsetMatch match = detector.detect();

            return match.getName();
        } catch (Exception e) {
            SoapUI.logError(e);
            return null;
        }
    }
}
