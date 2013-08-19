package com.eviware.soapui.impl.wsdl.panels.request;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
* Helper to find Swing/AWT components in a Container.
*/
public class ContainerWalker
{

	private java.util.List<Component> containedComponents;

	public ContainerWalker( Container container )
	{
		containedComponents = findAllComponentsIn( container );
	}

	public AbstractButton findButtonWithIcon(String iconFile) {
		for( Component component : containedComponents )
		{
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton )component;
				System.out.println(button);
				if (String.valueOf(button.getIcon()).endsWith( iconFile )) {
					return button;
				}
			}
		}
		throw new NoSuchElementException("No button found with icon file " + iconFile);
	}

	private java.util.List<Component> findAllComponentsIn( Container container )
	{
		java.util.List<Component> components = new ArrayList<>();
		for( Component component : container.getComponents() )
		{
			components.add(component);
			if (component instanceof Container) {
				components.addAll(findAllComponentsIn( (Container) component));
			}
		}
		return components;
	}
}
