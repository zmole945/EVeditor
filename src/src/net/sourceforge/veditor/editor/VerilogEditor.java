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

import java.io.StringReader;
import java.util.ResourceBundle;

import net.sourceforge.veditor.parser.ModuleList;
import net.sourceforge.veditor.parser.Segment;
import net.sourceforge.veditor.parser.VerilogParser;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 *  main class
 */
public class VerilogEditor extends TextEditor
{
	private ColorManager colorManager;
	private VerilogContentOutlinePage outlinePage;

	public VerilogEditor()
	{
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new VerilogSourceViewerConfiguration(colorManager));
		setDocumentProvider(new VerilogDocumentProvider());
		VerilogColorConstants.init();
	}

	public void updatePartControl(IEditorInput input)
	{
		super.updatePartControl(input);
		if (outlinePage != null)
			outlinePage.setInput(input);
	}

	public IDocument getDocument()
	{
		return getDocumentProvider().getDocument(getEditorInput());
	}

	protected void createActions()
	{
		super.createActions();

		IAction action;
		action =
			new TextOperationAction(
				VerilogEditorMessages.getResourceBundle(),
				"ContentAssistProposal.",
				this,
				ISourceViewer.CONTENTASSIST_PROPOSALS);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action);

		action = new GotoMatchingBracketAction();
		action.setActionDefinitionId(IJavaEditorActionDefinitionIds.GOTO_MATCHING_BRACKET);
		setAction("GotoMatchingBracket", action);

		action = new OpenDeclarationAction();
		action.setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_EDITOR);
		setAction("OpenDeclaration", action);
	}

//	����̓R���e���c�A�V�X�g�̂��߂̃R�[�h���Ǝv���邪�s�v���H
//	public void editorContextMenuAboutToShow(MenuManager menu)
//	{
//		super.editorContextMenuAboutToShow(menu);
//		addAction(menu, "ContentAssistProposal");
//		addAction(menu, "ContentAssistTip");
//	}

	public void dispose()
	{
		colorManager.dispose();
		if (outlinePage != null)
			outlinePage.setInput(null);
		super.dispose();
	}

	public void doRevertToSaved()
	{
		super.doRevertToSaved();
		if (outlinePage != null)
			outlinePage.update();
	}

	public void doSave(IProgressMonitor monitor)
	{
		super.doSave(monitor);
		if (outlinePage != null)
			outlinePage.update();
	}

	public void doSaveAs()
	{
		super.doSaveAs();
		if (outlinePage != null)
			outlinePage.update();
	}

	public void doSetInput(IEditorInput input) throws CoreException
	{
		super.doSetInput(input);
		if (outlinePage != null)
			outlinePage.setInput(input);
	}
	private IProject findProject(IEditorInput input)
	{
		if (input instanceof IFileEditorInput)
		{
			IFile file = ((IFileEditorInput)input).getFile();
			IContainer parent = file.getParent();
			while (parent instanceof IFolder)
			{
				parent = parent.getParent();
			}
			if (parent instanceof IProject)
			return (IProject)parent;
		}
		return null;
	}

	public Object getAdapter(Class required)
	{
		if (IContentOutlinePage.class.equals(required))
		{
			if (outlinePage == null)
			{
				outlinePage =
					new VerilogContentOutlinePage(getDocumentProvider(), this);
				if (getEditorInput() != null)
					outlinePage.setInput(getEditorInput());
			}
			return outlinePage;
		}
		return super.getAdapter(required);
	}

	public VerilogDocument getVerilogDocument()
	{
		IDocument doc = getDocument();
		if (doc instanceof VerilogDocument)
			return (VerilogDocument)doc;
		else
			return null;
	}

	protected void initializeEditor()
	{
		super.initializeEditor();

//		����̓R���e���c�A�V�X�g�̂��߂̃R�[�h���Ǝv����
//		setEditorContextMenuId("#EditorContext");
//		setRulerContextMenuId("#RulerContext");
	}

	protected void editorContextMenuAboutToShow(IMenuManager menu)
	{
		super.editorContextMenuAboutToShow(menu);
		menu.add(new Separator());
		menu.add(getAction("OpenDeclaration"));
	}

	private void beep()
	{
		Display.getCurrent().beep();
	}

	public Segment[] parse()
	{
		VerilogDocument doc = getVerilogDocument();
		if (doc != null)
		{
			VerilogParser parser = new VerilogParser(new StringReader(doc.get()));
			parser.parse(((VerilogDocument)doc).getProject());

			int size = parser.size();
			Segment[] elements = new Segment[size];
			for (int i = 0; i < size; i++)
				elements[i] = parser.getModule(i);
			return elements;
		}
		return null;
	}

	/**
	 * jamp bracket which includes begin/end
	 */
	public class GotoMatchingBracketAction extends Action
	{
		public static final String ID =
			"verilog.VerilogEditor.GotoMatchingBracketAction";
		public GotoMatchingBracketAction()
		{
			setEnabled(true);
			setId("ID");
			ResourceBundle resource = VerilogEditorMessages.getResourceBundle();
			setText(resource.getString("GotoMatchingBracket.label"));
		}
		public void run()
		{
			StyledText widget = getSourceViewer().getTextWidget();
			String text = widget.getText();

			int pos = widget.getCaretOffset();
			String[] open = { "(", "{", "[", "begin" };
			String[] close = {  ")", "}", "]", "end" };

			int openIdx = searchWord(open, text, pos);
			int closeIdx = searchWord(close, text, pos);

			int refPos = -1;
			if (openIdx != -1)
			{
				refPos = searchCloseBracket(text, pos, open[openIdx], close[openIdx]);
			}
			else if (closeIdx != -1)
			{
				refPos = searchOpenBracket(text, pos, open[closeIdx], close[closeIdx]);
			}
			if (refPos >= 0)
			{
				widget.setSelection(refPos);
				return;
			}
			beep();
		}

		/**
		 * open/close�̃L�[���[�h��T��
		 * @param words	�L�[���[�h�z��
		 * @param text		�Q�ƃe�L�X�g
		 * @param pos		�Q�ƈʒu�i��������O���Ɍ����j
		 * @return			�L�[���[�h�z��̃C���f�b�N�X
		 */
		private int searchWord(String[] words, String text, int pos)
		{
			for (int i = 0; i < words.length; i++)
			{
				int len = words[i].length();
				if (text.substring(pos - len, pos).equals(words[i]))
					return i;
			}
			return -1;
		}

		/**
		 * ���銇�ʂ�T���i�l�X�g�ɑΉ�����j
		 * @param text		�Q�ƃe�L�X�g
		 * @param pos		�����J�n�ʒu
		 * @param open		�J�����ʂ̕���
		 * @param close	�J���銇�ʂ̕���
		 * @return
		 */
		private int searchCloseBracket(String text, int pos, String open, String close)
		{
			int level = 1;
			int len = text.length();
			pos++;
			int openLen = open.length();
			int closeLen = close.length();
			while (pos < len)
			{
				String ref = text.substring(pos, pos + 1);
				if (testBracket(text, pos, open, openLen))
					level++;
				if (testBracket(text, pos, close, closeLen))
					level--;
				if (level == 0)
					return pos + closeLen;
				pos++;
			}
			return -1;
		}

		/**
		 * �J�����ʂ�T���i�l�X�g�ɑΉ�����j
		 * @param text		�Q�ƃe�L�X�g
		 * @param pos		�����J�n�ʒu
		 * @param open		�J�����ʂ̕���
		 * @param close	�J���銇�ʂ̕���
		 * @return
		 */
		private int searchOpenBracket(String text, int pos, String open, String close)
		{
			int level = 1;
			int openLen = open.length();
			int closeLen = close.length();
			pos -= 1 + closeLen;
			while (pos >= 0)
			{
				if (testBracket(text, pos, open, openLen))
					level--;
				if (testBracket(text, pos, close, closeLen))
					level++;
				if (level == 0)
					return pos + openLen;
				pos--;
			}
			return -1;
		}

		/**
		 * �Ή����銇�ʂ��e�X�g����
		 * @param text
		 * @param pos
		 * @param bracket
		 * @param len
		 * @return
		 */
		private boolean testBracket(String text, int pos, String bracket, int len)
		{
			if (bracket.equals(text.substring(pos, pos + len)))
			{
				if (Character.isJavaIdentifierStart(bracket.charAt(0)))
				{
					return !Character.isJavaIdentifierPart(text.charAt(pos - 1))
						&& !Character.isJavaIdentifierPart(text.charAt(pos + len));
				}
				else
					return true;
			}
			else
				return false;
		}
	}

	/**
	 * �v���W�F�N�g�̃c���[�̒����烂�W���[���̒�`�t�@�C����T��<p>
	 * ���W���[�����ƃt�@�C�����͓����łȂ���΂Ȃ�Ȃ�
	 */
	public class OpenDeclarationAction extends Action
	{
		public static final String ID =
			"verilog.VerilogEditor.OpenDeclarationAction";
		public OpenDeclarationAction()
		{
			setEnabled(true);
			setId("ID");
			ResourceBundle resource = VerilogEditorMessages.getResourceBundle();
			setText(resource.getString("OpenDeclaration.label"));
		}
		public void run()
		{
			StyledText widget = getSourceViewer().getTextWidget();

			String modName = widget.getSelectionText();
			if (modName.equals(""))
			{
				beep();
				return ;
			}

			ModuleList mlist = ModuleList.find(getVerilogDocument().getProject());
			IFile file = mlist.findFile(modName + ".v");
			if (file == null)
			{
				beep();
				return ;
			}
			openEditor(modName, file);
		}

		private void openEditor(String modName, IFile file)
		{
			IWorkbenchPage page =
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try
			{
				IEditorPart editorPart = page.openEditor(file);
				if (editorPart instanceof VerilogEditor)
				{
					VerilogEditor editor = (VerilogEditor)editorPart ;
					Segment[] modules = editor.parse();
					if (modules != null)
					{
						for (int i = 0; i < modules.length; i++)
						{
							Segment mod = modules[i] ;
							if (modName.equals(mod.toString()))
							{
								IDocument doc = editor.getDocument();
								int line = mod.getLine() - 1 ;
								int start = doc.getLineOffset(line);
								editor.getSourceViewer().getTextWidget().setSelection(start);
							}
						}
					}
				}
			}
			catch (BadLocationException e)
			{
			}
			catch (PartInitException e)
			{
			}
		}
	}
}


