/**
 * 
 */
package org.hupo.psi.mi.psicquic.freemarker.method;

import java.util.List;

import javax.annotation.Resource;



import org.hupo.psi.mi.psicquic.ols.client.OntologyTerm;
import org.hupo.psi.mi.psicquic.ols.client.SelfDiscoveringOntologyTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * @author Erik Pfeiffenberger
 *
 */

public class TermName implements TemplateMethodModel {

	SelfDiscoveringOntologyTree miOntologyTree;
	

	
	public SelfDiscoveringOntologyTree getMiOntologyTree() {
		return miOntologyTree;
	}

	public void setMiOntologyTree(SelfDiscoveringOntologyTree miOntologyTree) {
		this.miOntologyTree = miOntologyTree;
	}

	public TermName(SelfDiscoveringOntologyTree miOntologyTree) {
		this.miOntologyTree = miOntologyTree;
		
		System.out.println("TERM NAME: constructor called");
	}
	
	@Override
	public Object exec(List arg0) throws TemplateModelException {
		String termName = "dummy";
		if(arg0.size() != 1){
			throw new TemplateModelException("Wrong arguments");

		} else {
			String id = (String)arg0.get(0);
			
			
			OntologyTerm term = miOntologyTree.getTerm(id);
			
			termName = new String(term.getName());
		}
		return termName;
	}

}
