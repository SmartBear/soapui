package com.eviware.soapui.model.support;

import javax.swing.ImageIcon;

import com.eviware.soapui.config.ModelItemConfig;

public abstract class AbstractAnimatableModelItem<T extends ModelItemConfig> extends AbstractModelItem
{

	public abstract void setIcon( ImageIcon icon );

}
