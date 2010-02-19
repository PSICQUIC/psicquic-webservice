package org.hupo.psi.mi.psicquic.expressionTree;

import org.hupo.psi.mi.psicquic.ols.client.SelfDiscoveringOntologyTree;
import org.hupo.psi.mi.psicquic.registry.ServiceType;

public class ExpressionAndNode implements ExpressionNode{
	private ExpressionNode leftChild;
	private ExpressionNode rightChild;
	
	public ExpressionAndNode(ExpressionNode leftChild, ExpressionNode rightChild) {
		this.leftChild = leftChild;
		this.rightChild = rightChild;
	}

	@Override
	public boolean evaluate(ServiceType service, SelfDiscoveringOntologyTree sdoTree) {
		
		return leftChild.evaluate(service, sdoTree) && rightChild.evaluate(service, sdoTree);
	}

	@Override
	public String getRepresentation() {
		return "AND";
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
