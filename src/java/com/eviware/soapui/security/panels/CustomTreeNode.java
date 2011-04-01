package com.eviware.soapui.security.panels;

public interface CustomTreeNode
{
	void setExpandedIcon(boolean exp);
	
	void updateLabel();
	
	void setSelected( boolean selected );
}
