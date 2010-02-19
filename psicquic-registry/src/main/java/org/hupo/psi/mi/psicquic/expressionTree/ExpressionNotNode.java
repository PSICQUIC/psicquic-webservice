package org.hupo.psi.mi.psicquic.expressionTree;

import org.hupo.psi.mi.psicquic.ols.client.SelfDiscoveringOntologyTree;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
/**
 * 
 */

/**
 * @author Erik Pfeiffenberger
 *
 */
public class ExpressionNotNode implements ExpressionNode{
	
	private ExpressionNode child;
	
	public ExpressionNotNode(ExpressionNode child) {
		this.child = child;
	}

	@Override
	public boolean evaluate(ServiceType service, SelfDiscoveringOntologyTree sdoTree) {
		
		return !child.evaluate(service, sdoTree);
	}

	public ExpressionNode getChild() {
		return child;
	}

	public void setChild(ExpressionNode child) {
		this.child = child;
	}

	@Override
	public String getRepresentation() {
		
		return "NOT";
	}

	@Override
	public ExpressionNode getLeftChild() {
		
		return getChild();
	}

	@Override
	public ExpressionNode getRightChild() {
		
		return null;
	}


}
