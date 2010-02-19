package org.hupo.psi.mi.psicquic.expressionTree;

import java.util.List;

import org.hupo.psi.mi.psicquic.ols.client.OntologyTerm;
import org.hupo.psi.mi.psicquic.ols.client.SelfDiscoveringOntologyTree;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
/**
 * 
 */

/**
 * @author Erik Pfeiffenberger
 *
 */
public class ExpressionMiNode implements ExpressionNode{
	
	private String miId;
	
	public ExpressionMiNode(String miId) {
	
		this.miId = miId.toLowerCase();
	}
	

	@Override
	public boolean evaluate(ServiceType service, SelfDiscoveringOntologyTree sdoTree) {
		boolean result = false;
		List<String> serviceTags = service.getTags();
		
		for(String sTag : serviceTags){
			sTag = sTag;
			
			if(sTag.equalsIgnoreCase(miId)){
				result = true;
				break;
			}
		}
		
		
		
		if(result == false){
			for(String sTag : serviceTags){
				if(sdoTree.isChildTag(miId, sTag)){
					result = true;
					break;
				}
			}
		}
		
		return result;
	}


	@Override
	public String getRepresentation() {
		
		return miId;
	}


	@Override
	public ExpressionNode getLeftChild() {
		
		return null;
	}


	@Override
	public ExpressionNode getRightChild() {
		
		return null;
	}



}
