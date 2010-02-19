/**
 * 
 */
package org.hupo.psi.mi.psicquic.expressionTree;

import org.hupo.psi.mi.psicquic.ols.client.SelfDiscoveringOntologyTree;
import org.hupo.psi.mi.psicquic.registry.ServiceType;

/**
 * @author Erik Pfeiffenberger
 *
 */
public class ExpressionFalseNode implements ExpressionNode{
	
	public ExpressionFalseNode() {
		// does nothing...
	}

	@Override
	public boolean evaluate(ServiceType service, SelfDiscoveringOntologyTree sdoTree) {
		
		return false;
	}

	@Override
	public String getRepresentation() {
		
		return "FALSE";
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
