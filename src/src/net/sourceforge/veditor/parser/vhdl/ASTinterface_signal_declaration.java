/**
 * 
 * This file is based on the VHDL parser originally developed by
 * (c) 1997 Christoph Grimm,
 * J.W. Goethe-University Frankfurt
 * Department for computer engineering
 *
 **/
package net.sourceforge.veditor.parser.vhdl;



/* Generated By:JJTree: Do not edit this line. ASTinterface_signal_declaration.java */
/* JJT: 0.3pre1 */

public class ASTinterface_signal_declaration extends SimpleNode {
  ASTinterface_signal_declaration(int id) {
    super(id);
  }
  
  public String[] getIdentifierList(){
	  return ((ASTidentifier_list)jjtGetChild(0)).getIdentifierNames();
  }
  
  public String getSubType(){
	  for(Node child:children){
		  if(child instanceof ASTsubtype_indication){
			  ASTsubtype_indication subtype= (ASTsubtype_indication)child; 
			  return subtype.getIdentifier();
		  }
	  }
	  return null;
  }
  
  public String getMode(){
	  for(Node child:children){
		  if(child instanceof ASTmode){
			  ASTmode mode=(ASTmode)child;
			  return mode.getMode();
		  }
	  }
	  return null;
  }
}
