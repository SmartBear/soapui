package com.eviware.soapui.support.editor;

public interface EditorLocation<T extends EditorDocument>
{
	public int getColumn();

	public int getLine();
}