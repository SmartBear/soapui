/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
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

import com.eviware.soapui.support.types.StringList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class StringUtils {
    public static final String NEWLINE = System.getProperty("line.separator");
    public static final char DEFAULT_FILENAME_WHITESPACE_CHAR = '-';

    public static String unquote(String str) {
        int length = str == null ? -1 : str.length();
        if (str == null || length == 0) {
            return str;
        }

        if (length > 1 && str.charAt(0) == '\"' && str.charAt(length - 1) == '\"') {
            str = str.substring(1, length - 1);
        }

        return str;
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.length() == 0 || str.trim().length() == 0;
    }

    public static int parseInt(String str, int defaultValue) {
        if (isNullOrEmpty(str)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static List<String> splitLines(String string) {
        try {
            ArrayList<String> list = new ArrayList<String>();

            LineNumberReader reader = new LineNumberReader(new StringReader(string));
            String s;
            while ((s = reader.readLine()) != null) {
                list.add(s);
            }
            return list;
        } catch (IOException e) {
            // I don't think this can really happen with a StringReader.
            throw new RuntimeException(e);
        }
    }

    public static String normalizeSpace(String str) {
        if (!isNullOrEmpty(str)) {
            StringTokenizer st = new StringTokenizer(str);
            if (st.hasMoreTokens()) {

                StringBuilder sb = new StringBuilder(str.length());
                while (true) {
                    sb.append(st.nextToken());
                    if (st.hasMoreTokens()) {
                        sb.append(' ');
                    } else {
                        break;
                    }
                }
                return sb.toString();
            } else {
                return "";
            }
        } else {
            return str;
        }
    }

    public static boolean hasContent(String str) {
        return str != null && str.trim().length() > 0;
    }

    public static String stripStartAndEnd(String s, String start, String end) {
        if (s.startsWith(start) && s.endsWith(end)) {
            return s.substring(start.length(), s.length() - end.length());
        } else {
            return s;
        }
    }

    public static Writer createSeparatedRow(Writer writer, StringList values, char separator, char quote)
            throws IOException {
        for (int c = 0; c < values.size(); c++) {
            String value = values.get(c);

            if (c > 0) {
                writer.append(separator);
            }

            if (quote > 0) {
                writer.append(quote);

                if (value != null) {
                    for (int i = 0; i < value.length(); i++) {
                        char ch = value.charAt(i);

                        if (ch == quote) {
                            writer.append('\\');
                        } else if (ch == '\\') {
                            writer.append('\\');
                        }

                        writer.append(ch);
                    }
                }

                writer.append(quote);
            } else if (value != null) {
                writer.append(value);
            }
        }

        return writer;
    }

    public static StringList readSeparatedRow(String row, char separator, char quote) {
        StringList result = new StringList();

        while (row != null && row.length() > 0) {
            if (row.startsWith(String.valueOf(quote))) {
                StringBuilder buf = new StringBuilder();
                char last = row.charAt(0);
                int ix = 1;
                while (ix < row.length()) {
                    char ch = row.charAt(ix);
                    if (ch == quote && last != '\\') {
                        result.add(buf.toString());
                        row = row.length() > ix + 1 ? row.substring(ix + 1) : null;
                        if (row != null && row.length() > 1 && row.charAt(0) == separator) {
                            row = row.substring(1);
                            ix = -1;
                        }
                        break;
                    } else if (ch != '\\' || last == '\\') {
                        buf.append(ch);
                    }

                    last = ch;
                    ix++;
                }

                if (row != null && ix == row.length()) {
                    result.add(row);
                    row = null;
                }
            } else {
                int ix = row.indexOf(separator);
                if (ix == -1) {
                    result.add(row);
                    row = null;
                } else {
                    result.add(row.substring(0, ix));
                    row = row.substring(ix + 1);
                }
            }
        }

        return result;
    }

    /**
     * replaces only white spaces from file name
     */
    public static String createFileName(String str, char whitespaceChar) {
        StringBuilder result = new StringBuilder();

        for (int c = 0; c < str.length(); c++) {
            char ch = str.charAt(c);

            if (Character.isWhitespace(ch) && whitespaceChar != 0) {
                result.append(whitespaceChar);
            } else if (Character.isLetterOrDigit(ch)) {
                result.append(ch);
            } else if (ch == whitespaceChar) {
                result.append(ch);
            }
        }

        return result.toString();
    }

    /**
     * replaces only white spaces from file name, uses the
     * DEFAULT_FILENAME_WHITESPACE_CHAR
     */

    public static String createFileName(String str) {
        return createFileName(str, DEFAULT_FILENAME_WHITESPACE_CHAR);
    }

    /**
     * replaces all non letter and non digit characters from file name
     *
     * @param str
     * @param replace
     * @return
     */
    public static String createFileName2(String str, char replace) {
        StringBuilder result = new StringBuilder();

        for (int c = 0; c < str.length(); c++) {
            char ch = str.charAt(c);

            if (Character.isLetterOrDigit(ch)) {
                result.append(ch);
            } else {
                result.append(replace);
            }
        }

        return result.toString();
    }

    public static String createXmlName(String str) {
        StringBuilder result = new StringBuilder();
        boolean skipped = false;

        for (int c = 0; c < str.length(); c++) {
            char ch = str.charAt(c);

            if (Character.isLetter(ch) || ch == '_' || ch == '-' || ch == '.') {
                if (skipped) {
                    result.append(Character.toUpperCase(ch));
                } else {
                    result.append(ch);
                }
                skipped = false;
            } else if (Character.isDigit(ch)) {
                result.append(ch);
                skipped = false;
            } else {
                skipped = true;
            }
        }

        String resultString = result.toString();
        return isValidXmlName(str) ? resultString : "_" + resultString;
    }

    private static boolean isValidXmlName(String str) {
        if (str.isEmpty() || str.toLowerCase().startsWith("xml")) {
            return false;
        }
        char firstCharacter = str.charAt(0);
        return Character.isLetter(firstCharacter) || firstCharacter == '_';
    }

    public static String[] merge(String[] incomingNames, String string) {
        StringList result = new StringList(incomingNames);
        result.add(string);
        return result.toStringArray();
    }

    public static String quote(String str) {
        if (str == null) {
            return str;
        }

        if (str.length() < 2 || !str.startsWith("\"") || !str.endsWith("\"")) {
            str = "\"" + str + "\"";
        }

        return str;
    }

    public static String join(String[] array, String separator) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buf.append(separator);
            }
            buf.append(array[i]);
        }
        return buf.toString();
    }

    public static String toHtml(String string) {
        return toHtml(string, 0);
    }

    public static String toHtml(String string, int maxSize) {
        if (StringUtils.isNullOrEmpty(string)) {
            return "<html><body></body></html>";
        }

        BufferedReader st = new BufferedReader(new StringReader(string));
        StringBuilder buf = new StringBuilder("<html><body>");

        String str = null;

        try {
            str = st.readLine();

            while (str != null && (maxSize == 0 || (buf.length() + str.length()) < maxSize)) {
                if (str.equalsIgnoreCase("<br/>")) {
                    str = "<br>";
                }

                buf.append(str);

                if (!str.equalsIgnoreCase("<br>")) {
                    buf.append("<br>");
                }

                str = st.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (str != null) {
            buf.append("...");
        }

        buf.append("</body></html>");
        string = buf.toString();
        return string;
    }

    public static String replace(String data, String from, String to) {
        StringBuilder buf = new StringBuilder(data.length());
        int pos;
        int i = 0;
        while ((pos = data.indexOf(from, i)) != -1) {
            buf.append(data.substring(i, pos)).append(to);
            i = pos + from.length();
        }
        buf.append(data.substring(i));
        return buf.toString();
    }

    public static String fixLineSeparator(String xml) throws UnsupportedEncodingException {
        if ("\r\n".equals(System.getProperty("line.separator"))) {
            xml = xml.replaceAll("\r[^\n]", System.getProperty("line.separator"));
        } else {
            xml = xml.replaceAll("\r\n", System.getProperty("line.separator"));
        }

        return xml;
    }

    public static String capitalize(String string) {
        if (isNullOrEmpty(string)) {
            return string;
        }
        return string.toUpperCase().substring(0, 1) + string.toLowerCase().substring(1);
    }

    public static String[] toStringArray(Object[] selectedOptions) {
        String[] result = new String[selectedOptions.length];
        for (int c = 0; c < selectedOptions.length; c++) {
            result[c] = String.valueOf(selectedOptions[c]);
        }
        return result;
    }

    public static List<String> toStringList(Object[] selectedOptions) {
        StringList result = new StringList();

        for (Object o : selectedOptions) {
            result.add(o.toString());
        }

        return result;
    }

    public static String[] sortNames(String[] names) {
        Arrays.sort(names);
        return names;
    }
}
