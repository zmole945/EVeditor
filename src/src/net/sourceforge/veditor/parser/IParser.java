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
package net.sourceforge.veditor.parser;

import java.io.Reader;

import org.eclipse.core.resources.IFile;

public interface IParser
{
	public static final int OUT_OF_MODULE = 0;
	public static final int IN_MODULE = 1;
	public static final int IN_STATEMENT = 2;

	public ParserManager getManager();
	public IFile getFile();
	public void parse() throws ParseException;
	public void parseLineComment(Reader reader);
}

