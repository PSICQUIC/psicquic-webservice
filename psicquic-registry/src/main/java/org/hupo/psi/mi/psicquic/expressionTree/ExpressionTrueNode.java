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
public class ExpressionTrueNode implements ExpressionNode{
	public ExpressionTrueNode() {
		// does nothing...
	}

	@Override
	public boolean evaluate(ServiceType service, SelfDiscoveringOntologyTree sdoTree) {
		
		return true;
	}

	@Override
	public String getRepresentation() {
		return "TRUE";
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
