package com.eviware.soapui.model.project;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.support.SoapUIException;

public interface ProjectFactory<T extends Project>
{
	public T createNew() throws XmlException, IOException, SoapUIException;
	public T createNew(String path) throws XmlException, IOException, SoapUIException;
	public T createNew(String projectFile, String projectPassword);
	public T createNew(Workspace workspace);
	public T createNew(String path, Workspace workspace);
	public T createNew(String path, Workspace workspace, boolean create);
	public T createNew(String path, Workspace workspace, boolean create, boolean open, String tempName, String projectPassword);
	
}
