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

import java.util.ArrayList;
import java.util.List;

/**
 * verilog�\�[�X�R�[�h���Z�O�����g��������
 */
public class VerilogSegment
{
	private int line ;
	private int length ;
	private VerilogSegment parent ;	//  ��`���̃��W���[��
	private String name ;				//  module���܂���function��
	private String iname ;			//  �C���X�^���X��

	private List refs = new ArrayList();

	//  ���W���[����`
	public VerilogSegment( int line, String name )
	{
		this( line, name, null );
		refs = new ArrayList();
	}

	//  ���W���[���Q��
	private VerilogSegment( int line, String name, String inst )
	{
		this.line = line ;
		this.parent = null ;
		this.name = name ;
		this.iname = inst ;
		this.length = 1 ;
		refs = null ;
	}

	public void setEndLine( int line )
	{
		length = line - this.line + 1 ;
	}
	public void addReference( int begin, int end, String mod, String inst )
	{
		VerilogSegment module = new VerilogSegment( begin, mod, inst );
		module.parent = this ;
		module.length = end - begin + 1 ;
		refs.add( module );
	}
	public void addComment( int begin, String str )
	{
		if ( ! isValidComment( str ) )
			return ;
			
		//  �R�����g�s���A�����Ă���ꍇ�͍ŏ��ő�\������
		if ( begin == lastCommentLine + 1 )
		{
			lastCommentLine = begin ;
			return ;
		}
		
		//  �s�̍ŏ���"//"�Ɩ��ʂȕ������폜����
		for( int i = 0 ; i < str.length() ; i++ )
		{
			char ch = str.charAt(i);
			if ( Character.isLetterOrDigit(ch) )
			{
				str = str.substring(i);
				break ;
			}
		}
		//  �s�̍Ō�̖��ʂȕ������폜����
		for( int i = str.length() - 1  ; i >= 0 ; i-- )
		{
			char ch = str.charAt(i);
			if ( Character.isLetterOrDigit(ch) )
			{
				str = str.substring(0,i+1);
				break ;
			}
		}

		VerilogSegment comment = new VerilogSegment( begin, "// ", str );
		comment.parent = this ;
		comment.length = 1 ;
		refs.add( comment );
		lastCommentLine = begin ;
	}
	private int lastCommentLine ;
	
	private boolean isValidComment( String str )
	{
		for( int i = 0 ; i < str.length() ; i++ )
		{
			char ch = str.charAt(i);
			if ( Character.isLetterOrDigit(ch) )
				return true ;
		}
		return false ;
	}
	
	
	public VerilogSegment getParent()
	{
		return parent ;
	}
	public VerilogSegment getInstance( int n )
	{
		return (VerilogSegment)refs.get( n );
	}
	public int size()
	{
		if ( refs != null )
			return refs.size();
		else
			return 0;
	}
	public String toString()
	{
		if ( iname == null )
			return name ;
		else
			return name + " " + iname ;
	}
	public int getLine()
	{
		return line;
	}
	public int getLength()
	{
		return length;
	}

}
