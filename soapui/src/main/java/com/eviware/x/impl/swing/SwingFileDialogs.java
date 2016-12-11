/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
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

package com.eviware.x.impl.swing;

import com.eviware.soapui.support.ExtensionFileFilter;
import com.eviware.x.dialogs.XFileDialogs;

import javax.swing.JFileChooser;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lars
 */
public class SwingFileDialogs implements XFileDialogs {
    private static Component parent;
    private static Map<Object, JFileChooser> choosers = new HashMap<Object, JFileChooser>();

    public SwingFileDialogs(Component parent) {
        SwingFileDialogs.parent = parent;
    }

    public static synchronized JFileChooser getChooser(Object action) {
        action = null;
        JFileChooser chooser = choosers.get(action);
        if (chooser == null) {
            chooser = new JFileChooser();
            choosers.put(action, chooser);
        }

        chooser.resetChoosableFileFilters();

        return chooser;
    }

    public static Component getParent() {
        return parent;
    }

    public File saveAs(Object action, String title) {
        return saveAs(action, title, null, null, null);
    }

    public File saveAs(Object action, String title, String extension, String fileType, File defaultFile) {
        JFileChooser chooser = getChooser(action);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle(title);
        chooser.setAcceptAllFileFilterUsed(true);

        if (extension != null && fileType != null) {
            chooser.setFileFilter(new ExtensionFileFilter(extension, fileType));
        } else {
            chooser.setFileFilter(null);
        }

        if (defaultFile != null) {
            chooser.setSelectedFile(defaultFile);
        } else {
            chooser.setSelectedFile(null);
        }

        if (chooser.showSaveDialog(getParent()) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return chooser.getSelectedFile();
    }

    public File open(Object action, String title, String extension, String fileType, String current) {
        return openFile(action, title, extension, fileType, current);
    }

    public static File openFile(Object action, String title, String extension, String fileType, String current) {
        JFileChooser chooser = getChooser(action);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle(title);
        chooser.setAcceptAllFileFilterUsed(true);
        if (current != null) {
            File file = new File(current);
            if (file.isDirectory()) {
                chooser.setCurrentDirectory(file);
            } else {
                chooser.setSelectedFile(file);
            }
        } else {
            chooser.setSelectedFile(null);
        }

        if (extension != null && fileType != null) {
            chooser.setFileFilter(new ExtensionFileFilter(extension, fileType));
        } else {
            chooser.setFileFilter(null);
        }

        if (chooser.showOpenDialog(getParent()) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return chooser.getSelectedFile();
    }

    public File openXML(Object action, String title) {
        return open(action, title, ".xml", "XML Files (*.xml)", null);
    }

    public File openDirectory(Object action, String title, File defaultDirectory) {
        JFileChooser chooser = new JFileChooser(defaultDirectory);
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setPreferredSize(new Dimension(400, 400));
        if (chooser.showSaveDialog(getParent()) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return chooser.getSelectedFile();

        // JFileChooser chooser = getChooser( action );
        // chooser.setDialogTitle( title );
        // chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        //
        // if( defaultDirectory != null )
        // chooser.setCurrentDirectory( defaultDirectory );
        //
        // if( chooser.showOpenDialog( getParent() ) !=
        // JFileChooser.APPROVE_OPTION )
        // return null;
        //
        // return chooser.getSelectedFile();
    }

    public File openFileOrDirectory(Object action, String title, File defaultDirectory) {
        JFileChooser chooser = getChooser(action);
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        if (defaultDirectory != null) {
            chooser.setCurrentDirectory(defaultDirectory);
        }

        if (chooser.showOpenDialog(getParent()) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return chooser.getSelectedFile();
    }

    public File saveAsDirectory(Object action, String title, File defaultDirectory) {
        JFileChooser chooser = new JFileChooser(defaultDirectory);
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(true);
        if (chooser.showSaveDialog(getParent()) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return chooser.getSelectedFile();

        // JFileChooser chooser = getChooser( action );
        // chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        // chooser.setDialogTitle( title );
        // chooser.setAcceptAllFileFilterUsed( true );
        //
        // if( defaultDirectory != null )
        // chooser.setSelectedFile( defaultDirectory );
        // else
        // chooser.setSelectedFile( null );
        //
        // if( chooser.showSaveDialog( getParent() ) !=
        // JFileChooser.APPROVE_OPTION )
        // return null;
        //
        // return chooser.getSelectedFile();
    }
}
