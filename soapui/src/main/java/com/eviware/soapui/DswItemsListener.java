/*
 * Copyright (C) 1995-2020 Die Software Peter Fitzon GmbH
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

package com.eviware.soapui;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.actions.DswImportWsdlProjectAction;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.model.workspace.WorkspaceListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.DefaultActionMapping;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.ui.desktop.DesktopListener;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Workspace/Deskopt Listener that updates the recent menus..
 *
 * @author Jovan Vasic
 */

public class DswItemsListener implements WorkspaceListener, DesktopListener {
    
    private JMenu openMenu;
    
    public DswItemsListener(JMenu openMenu) {
        this.openMenu = openMenu;
        
        openMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {

            public void popupMenuCanceled(PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }
        });
        
        Map<String,String> envMap = getDswProjects();
        if (envMap != null) {
	        String wsName = "";
	        String filePath = "";
	        Set<Entry<String, String>> set = envMap.entrySet();
	        Iterator<Entry<String, String>> iterator = set.iterator();
	        while(iterator.hasNext()) {
	           Entry<String, String> entry = iterator.next();
	           wsName = entry.getKey();
	           filePath = entry.getValue();
	           
	           // Open Action 
	           DefaultActionMapping<WorkspaceImpl> openMapping = new DefaultActionMapping<WorkspaceImpl>(
	        		   DswImportWsdlProjectAction.SOAPUI_ACTION_ID, null, null, false, filePath);
	           openMapping.setName(wsName);
	           openMapping.setDescription("Open project " + wsName + " - " + filePath);
	
	           @SuppressWarnings({ "rawtypes", "unchecked" })
	           AbstractAction delegate = new SwingActionDelegate(openMapping, SoapUI.getWorkspace());
	           this.openMenu.add(new JMenuItem(delegate));
	        }
        }
    }
	
	private Map<String,String> getDswProjects() {
		// First check JAR directory for DSW project config file
		String path = "./dsw_soapui_projects.xml";
		InputStream input = null;
		try {
			
			try {
				input = new FileInputStream(path);
			} catch (FileNotFoundException e) {
				UISupport.showInfoMessage("Configuration file dsw_soapui_projects.xml does not exist in application folder, DSW menu will not be created");
			} 
			
			if (input != null) {
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder;
				Document document = null;
				try {
					documentBuilder = documentBuilderFactory.newDocumentBuilder();
					document = documentBuilder.parse(input);
					document.getDocumentElement().normalize();
				} catch (ParserConfigurationException | SAXException | IOException e) {
					e.printStackTrace();
				}
				
				Map<String,String> envMap = new LinkedHashMap<String, String>();
		        NodeList nodeList = document.getElementsByTagName("item");
		         
		        for (int i=0; i < nodeList.getLength(); i++) {
		            Node node = nodeList.item(i);
		            if (node.getNodeType() == Node.ELEMENT_NODE) {
		            	Element element = (Element) node;
		            	String itemName = element.getAttribute("name");
		            	String itemPath = element.getAttribute("path");
		            	
		            	envMap.put(itemName,itemPath);
		            }
		        }
		        return envMap;
			}
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	@Override
	public void desktopPanelSelected(DesktopPanel desktopPanel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void desktopPanelCreated(DesktopPanel desktopPanel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void desktopPanelClosed(DesktopPanel desktopPanel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void projectAdded(Project project) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void projectRemoved(Project project) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void projectChanged(Project project) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void projectOpened(Project project) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void projectClosed(Project project) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void workspaceSwitching(Workspace workspace) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void workspaceSwitched(Workspace workspace) {
		// TODO Auto-generated method stub
		
	}
    
}
