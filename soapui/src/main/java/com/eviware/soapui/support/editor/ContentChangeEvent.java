/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.support.editor;

import com.eviware.soapui.impl.wsdl.submit.transports.http.DocumentContent;

public class ContentChangeEvent {
    private DocumentContent newContent;
    private DocumentContent oldContent;

    public ContentChangeEvent(DocumentContent oldContent, DocumentContent newContent) {
        //To change body of created methods use File | Settings | File Templates.
        this.oldContent = oldContent;
        this.newContent = newContent;
    }

    public DocumentContent getOldContent() {
        return oldContent;
    }

    public DocumentContent getNewContent() {
        return newContent;
    }
}
