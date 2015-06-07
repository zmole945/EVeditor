/*******************************************************************************
 * Copyright (c) 2004, 2006 KOBAYASHI Tadashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    KOBAYASHI Tadashi - initial API and implementation
 *******************************************************************************/
package net.sourceforge.veditor.document;

import java.util.Vector;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.editor.scanner.HdlPartitionScanner;
import net.sourceforge.veditor.parser.HdlParserException;
import net.sourceforge.veditor.parser.IParser;
import net.sourceforge.veditor.parser.OutlineContainer;
import net.sourceforge.veditor.parser.OutlineDatabase;
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.VariableStore;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.PackageDeclElement;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;

abstract public class HdlDocument extends Document
{
	/**
	 * project which has this verilog source file
	 */
	private IProject m_Project;
	private IFile m_File;	
	private boolean m_NeedToRefresh;
	private VariableStore variableStore;

	public HdlDocument(IProject project, IFile file)
	{
		super();
		m_Project = project;
		m_File = file;
		m_NeedToRefresh=true;
		addDocumentListener(new HdlDocumentListner());		
	}

	public IProject getProject()
	{
		return m_Project;
	}

	public IFile getFile()
	{
		return m_File;
	}

	/**
	 * Gets the outline database object for this project	 
	 * @return
	 */
	public OutlineDatabase getOutlineDatabase(){	
			OutlineDatabase database = null;
			IProject project = getProject();
			if(project != null){
				try {
					database = (OutlineDatabase) project
							.getSessionProperty(VerilogPlugin.getOutlineDatabaseId());
					if(database == null){
						database=CreateOutlineDatabase(project);
					}
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return database;
		}
	
	/**
	 * Same as  getOutlineContainer(true);
	 * @return
	 * @throws HdlParserException 
	 */
	public OutlineContainer getOutlineContainer() throws HdlParserException{
		return getOutlineContainer(true);
	}
	/**
	 * @param bRefreshIfNeeded if true the outline will be if the data is stale
	 * @return The outline container for this document
	 * @throws HdlParserException 
	 */
	public OutlineContainer getOutlineContainer(boolean bRefreshIfNeeded) throws HdlParserException{
		OutlineDatabase database=getOutlineDatabase();
		if (database == null){
			return null;
		}
		OutlineContainer results=database.getOutlineContainer(getFile());
		if(results==null || bRefreshIfNeeded){
			refreshOutline();
		}
		
		return database.getOutlineContainer(getFile());
	}
	
	/**
	 * Refreshes the outline database if necessary
	 * @return true if a refresh was required
	 * @throws HdlParserException 
	 */
	public boolean refreshOutline() throws HdlParserException{
		if(m_NeedToRefresh){
			m_NeedToRefresh=false;
			getOutlineContainer(false).clear();
			IParser parser = createParser(get());
			VerilogPlugin.deleteMarkers(getFile());
			try{
				parser.parse();
			}
			catch (HdlParserException e){
				throw e;
			}
			OutlineDatabase database = OutlineDatabase.getProjectsDatabase(getProject());
			database.scanTree(getFile());
			variableStore = parser.getVariableStore();
			return true;
		}		
		return false;
	}
	
	public VariableStore getVariableStore() {
		try {
			refreshOutline();
		} catch (HdlParserException e) {
			return null;
		}
		return variableStore;
	}

	/**
	 * Used to listen for document changes
	 *
	 */
	private class HdlDocumentListner implements IDocumentListener{

		public void documentAboutToBeChanged(DocumentEvent event) {
			// TODO Auto-generated method stub
			
		}

		public void documentChanged(DocumentEvent event) {
			//skip over the first modification because it is usually fired when a save occurs
			if(m_NeedToRefresh==false && event.getModificationStamp() > 1){
				m_NeedToRefresh=true;				
			}
		}
		
	}
	
		
	/**
	 * Returns the element near the given document offset
	 * @param document
	 * @param doRefresh If set to true, the document will be 
	 * parsed (if dirty) before an attempt is made to find the element
	 * @return
	 * @throws HdlParserException 
	 */
	public OutlineElement getElementAt(int documentOffset,boolean doRefresh)  throws BadLocationException, HdlParserException{
		int line=getLineOfOffset(documentOffset);
		int col=documentOffset-getLineOffset(line);
		
		return getOutlineContainer(doRefresh).getLineContext(line, col);
	}
	
	
	/**
	 * Creates an outline database and adds it to the project
	 * if one does not exist. This function will do useful work
	 * once per project	 
	 * @param project Project to owning the data base	
	 *   
	 */
	private OutlineDatabase CreateOutlineDatabase(IProject project){
		// do we already have an outline database?
		OutlineDatabase database=null;
		try {
			database = (OutlineDatabase)project.getSessionProperty(VerilogPlugin.getOutlineDatabaseId());			
		} catch (CoreException e) {			
			e.printStackTrace();
		}
		//if not created yet, make one
		if(database == null){
			database=new OutlineDatabase(project);
			try {
				project.setSessionProperty(VerilogPlugin.getOutlineDatabaseId(), database);
				database.scanProject();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return database;
	}
	
	/**
	 * Gets the indent string of the line that contains the offset
	 * @param documentOffset offset from the beginning of the document
	 * @return Indent string
	 */
	public  String getIndentString(int documentOffset)
	{
		try
		{
			int line = getLineOfOffset(documentOffset);
			int pos = getLineOffset(line);
			StringBuffer buf = new StringBuffer();
			for (;;)
			{
				char c = getChar(pos++);
				if (!Character.isSpaceChar(c) && c != '\t')
					break;
				buf.append(c);
			}
			return buf.toString();
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public Vector<OutlineElement> getPackageElementByName(String name, boolean useOnlyName, int offset) {
		Vector<OutlineElement> results = new Vector<OutlineElement>();
		
		String segments[]=name.split("[(]");
		String splitNames[]=segments[0].split("[.]");
		
		String keyword = splitNames[0].trim();
		String packageName = "";
		boolean exactName = false;
		if ((splitNames.length == 3) && splitNames[0].equalsIgnoreCase("work")) {
			keyword = splitNames[2].trim();
			packageName = splitNames[1].trim();
			exactName = true;
		}
		
		OutlineDatabase database = getOutlineDatabase();
		if (database != null) {
			PackageDeclElement[] packages = database.findTopLevelPackages();
			Vector<OutlineElement> subPackageHits = new Vector<OutlineElement>();
			
			// find all the possible elements in the line
			for (int i = 0; i < packages.length; i++) {
				if(packages[i] instanceof PackageDeclElement ){
					OutlineElement[] subPackageElements=packages[i].getChildren();
					for(int j=0; j< subPackageElements.length; j++){
						if ( subPackageElements[j].getName()
										.equalsIgnoreCase(keyword)) {
							// only add the found declarations here
							subPackageHits.add(subPackageElements[j]);
						}
					}
				}
			}
			
			if (exactName) {
				for (int i = 0; i < subPackageHits.size(); i++) {
					// look if the name of the package is there
					String parentName = subPackageHits.get(i).getParent().getName();
					
					// compare with the package name
					if (parentName.equalsIgnoreCase(packageName)) {
						results.add(subPackageHits.get(i));
					}
				}
			}
			
			// check if there is a package name is front of it and use it
			if ((!useOnlyName) && (!exactName)) {
				for (int i = 0; i < subPackageHits.size(); i++) {
					// look if the name of the package is there
					String parentName = subPackageHits.get(i).getParent().getName() + ".";
					int nwOffset = offset - parentName.length();
					// get the length of the name leading to the selection
					String leadingString = "";
					try {
						leadingString = get(nwOffset, parentName.length());
					} catch (BadLocationException e) {
					}
					
					// compare with the package name
					if (leadingString.equalsIgnoreCase(parentName)) {
						results.add(subPackageHits.get(i));
					}
				}
			}
			
			if (!exactName) {
				// look if on of the found elements is declared under the uses
				if (results.size() == 0) {
					OutlineContainer docContainer = new OutlineContainer();
					try {
						docContainer = getOutlineContainer(false);
					} catch (HdlParserException e) {
					}
					
					for (int i = 0; i < subPackageHits.size(); i++) {
						OutlineElement useElement = docContainer.findTopLevelElement(
							"work." + subPackageHits.get(i).getParent().getName());
						if (useElement != null) {
							results.add(subPackageHits.get(i));
						}
					}
				}
			}
			
			if ((!exactName) || (subPackageHits.size() == 0)) {
				// no matching use clause found, so open all matching elements 
				if (results.size() == 0) {
					results = subPackageHits;
				}
			}
		}
		
		return results;
	}
	
	abstract public HdlPartitionScanner createPartitionScanner();
	abstract protected IParser createParser(String text);
	public abstract Vector<OutlineElement> getDefinitionList(String name,int offset);	
	/**
	 * returns the context of the given offset
	 * @param documentOffset
	 * @return
	 */
	public abstract int getContext(int documentOffset) throws BadLocationException;

}



