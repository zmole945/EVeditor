//
//  Copyright 2004, KOBAYASHI Tadashi
//  $Id$
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//

package net.sourceforge.veditor.editor;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class VerilogEditorMessages
{
	private static final String RESOURCE_BUNDLE =
		"net.sourceforge.veditor.editor.VerilogEditorMessages";

	private static ResourceBundle resourceBundle =
		ResourceBundle.getBundle(RESOURCE_BUNDLE);

	private VerilogEditorMessages()
	{
	}

	public static String getString(String key)
	{
		try
		{
			return resourceBundle.getString(key);
		}
		catch (MissingResourceException e)
		{
			return "!" + key + "!";
		}
	}

	public static ResourceBundle getResourceBundle()
	{
		return resourceBundle;
	}
}
