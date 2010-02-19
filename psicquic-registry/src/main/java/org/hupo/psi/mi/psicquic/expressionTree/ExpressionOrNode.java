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
public class ExpressionOrNode implements ExpressionNode{
	
	private ExpressionNode leftChild;
	private ExpressionNode rightChild;
	
	public ExpressionOrNode(ExpressionNode leftChild, ExpressionNode rightChild) {
		this.leftChild = leftChild;
		this.rightChild = rightChild;
	}

	@Override
	public boolean evaluate(ServiceType service, SelfDiscoveringOntologyTree sdoTree) {
		
		return leftChild.evaluate(service, sdoTree) || rightChild.evaluate(service, sdoTree);
	}

	@Override
	public String getRepresentation() {
		
		return "OR";
	}

	@Override
	public ExpressionNode getLeftChild() {
		
		return leftChild;
	}

	@Override
	public ExpressionNode getRightChild() {
		
		return rightChild;
	}



}
