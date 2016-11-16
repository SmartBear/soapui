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

package com.eviware.soapui.impl.wsdl.monitor;

import org.apache.commons.lang.StringUtils;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author joel.jonsson
 */
public class ContentTypes {
    private List<ContentType> contentTypes;

    private ContentTypes(List<ContentType> contentTypes) {
        this.contentTypes = contentTypes;
    }

    public static ContentTypes of(String contentTypes) {
        List<ContentType> contentTypeList = new ArrayList<ContentType>();
        for (String ct : contentTypes.split(",")) {
            try {
                contentTypeList.add(new ContentType(ct.trim()));
            } catch (ParseException ignore) {
            }
        }
        return new ContentTypes(contentTypeList);
    }

    public boolean matches(String value) {
        for (ContentType contentType : contentTypes) {
            try {
                ContentType respondedContentType = new ContentType(value);
                if (contentTypeMatches(contentType, respondedContentType)) {
                    return true;
                }
            } catch (ParseException ignore) {
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return StringUtils.join(contentTypes, ", ");
    }

    private boolean contentTypeMatches(ContentType contentType, ContentType respondedContentType) {
        // ContentType doesn't take wildcards into account for the primary type, but we want to do that
        return contentType.match(respondedContentType) ||
                ((contentType.getPrimaryType().charAt(0) == '*'
                        || respondedContentType.getPrimaryType().charAt(0) == '*')
                        && (contentType.getSubType().charAt(0) == '*'
                        || respondedContentType.getSubType().charAt(0) == '*'
                        || contentType.getSubType().equalsIgnoreCase(respondedContentType.getSubType())));
    }
}
