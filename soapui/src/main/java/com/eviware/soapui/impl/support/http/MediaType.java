/*
 * Copyright 2004-2014 SmartBear Software
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
package com.eviware.soapui.impl.support.http;

/**
 * This interface represents a MediaType. It can be used as a model for user interfaces that needs
 * to set a media type. For example a combo box of media types.
 */
public interface MediaType {
    /**
     * This method should return a valid media type. For example application/xml or
     * application/json. Check out the standard for valid values.
     *
     * @return a valid media type.
     */
    public String getMediaType();

    /**
     * This method should set the media type so it can later be accessed by getMediaType.
     *
     * @param mediaType a valid media type.
     */
    public void setMediaType(String mediaType);
}
