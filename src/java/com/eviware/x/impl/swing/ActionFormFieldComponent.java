package com.eviware.x.impl.swing;

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.JButton;

import com.eviware.x.form.XFormTextField;

public class ActionFormFieldComponent extends AbstractSwingXFormField<JButton> implements XFormTextField
{
	public ActionFormFieldComponent(String name, String description)
	{
		super( new JButton( name ) );
	}

	public void setWidth(int columns)
	{
		getComponent().setPreferredSize(new Dimension( columns, 20 ));
	}

	public String getValue()
	{
		return null;
	}

	public void setValue(String value)
	{
	}

	@Override
	public void setProperty(String name, Object value)
	{
		if( name.equals("action"))
		{
			getComponent().setAction((Action) value);
		}
		else
		{
			super.setProperty(name, value);
		}
	}
	
	
}
