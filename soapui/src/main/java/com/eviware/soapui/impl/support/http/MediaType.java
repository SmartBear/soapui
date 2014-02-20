package com.eviware.soapui.impl.support.http;

/**
 * This interface represents a MediaType. It can be used as a model for user interfaces that needs
 * to set a media type. For example a combo box of media types.
 */
public interface MediaType
{
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
	public void setMediaType( String mediaType );
}
