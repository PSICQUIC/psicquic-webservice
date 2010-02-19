/**
 * 
 */
package org.hupo.psi.mi.psicquic.expressionTree.test;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.hupo.psi.mi.psicquic.expressionTree.ExpressionScanner;
import org.hupo.psi.mi.psicquic.expressionTree.ExpressionTree;
import org.hupo.psi.mi.psicquic.expressionTree.ParseExpressionException;
import org.hupo.psi.mi.psicquic.freemarker.method.TermName;
import org.hupo.psi.mi.psicquic.ols.client.OLSClient;
import org.hupo.psi.mi.psicquic.ols.client.SelfDiscoveringOntologyTree;
import org.hupo.psi.mi.psicquic.ols.soap.Query;
import org.hupo.psi.mi.psicquic.ols.soap.QueryServiceLocator;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;





import freemarker.template.TemplateModelException;

/**
 * @author Erik Pfeiffenberger
 *
 */
public class Test {
//	@Autowired
//	public SelfDiscoveringOntologyTree sdoTree;
//
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		Test test = new Test();
//		
//		test.test1();
//
//	}
//	
//	public void runtests() throws ParseExpressionException{
//		System.out.println("Running all tests...");
//		System.out.println("------------------------\n\n");
//		
//		System.out.println("---Test 1----------------------");
//		test1();
//		System.out.println("\n-----------------------------");
//		
//		System.out.println("---Test 2----------------------");
//		test2();
//		System.out.println("\n-----------------------------");
//		
//		System.out.println("---Test 3----------------------");
//		test3();
//		System.out.println("\n-----------------------------");
//		
//		System.out.println("---Test 4----------------------");
//		test4();
//		System.out.println("\n-----------------------------");
//		
//
//		System.out.println("---Test 5----------------------");
//		test5();
//		System.out.println("\n-----------------------------");
//		
//		System.out.println("---Test 6----------------------");
//		test6();
//		System.out.println("\n-----------------------------");
//
//		System.out.println("---Test 7----------------------");
//		test7();
//		System.out.println("\n-----------------------------");
//		
//		System.out.println("---Test 8----------------------");
//		test8();
//		System.out.println("\n-----------------------------");
//		
//		System.out.println("---Test 9----------------------");
//		test9();
//		System.out.println("\n-----------------------------");
//		
//		System.out.println("---Test 10----------------------");
//		test10();
//		System.out.println("\n-----------------------------");
//		
//		
//	}
//	
//	/**
//	 * parse all symbols test
//	 */
//	public void test1(){
//		String text = "not ( mi:0000 and mi:\"omg lol\" )";
//		List<String> symbols = new ArrayList<String>();
//		ExpressionScanner scanner = new ExpressionScanner(text);
//		
//		while(scanner.hasNext()){
//			symbols.add(scanner.nextSymbol());
//		}
//		
//		for (String symbol : symbols){
//			System.out.println("Symbol=\""+symbol+"\"");
//		}
//		
//	}
//	/**
//	 * parse all symbols and test recognition...
//	 */
//	public void test2(){
//		String text = "()OR AND free text NOT MI:0001";
//		
//		ExpressionScanner scanner = new ExpressionScanner(text);
//		
//		System.out.println("Symbol=\""+scanner.nextSymbol()+"\" : "+scanner.isLeftPar());
//		System.out.println("Symbol=\""+scanner.nextSymbol()+"\" : "+scanner.isRightPar());
//		System.out.println("Symbol=\""+scanner.nextSymbol()+"\" : "+scanner.isOr());
//		System.out.println("Symbol=\""+scanner.nextSymbol()+"\" : "+scanner.isAnd());
//		System.out.println("Symbol=\""+scanner.nextSymbol()+"\" : "+scanner.isFreeText());
//		System.out.println("Symbol=\""+scanner.nextSymbol()+"\" : "+scanner.isNot());
//		System.out.println("Symbol=\""+scanner.nextSymbol()+"\" : "+scanner.isMiIdentifier());
//		
//	}
//	
//	public void test3() throws ParseExpressionException{
//		String expression = "MI:0959";
//		
//		List<String> tagList = new ArrayList<String>();
//		tagList.add("MI:0959");
//		
//		String allTagsAsStr = "";
//		
//		for (String t : tagList){
//			allTagsAsStr = allTagsAsStr + "\""+t+"\" ";
//		}
//		
//		List<String> tags = new ArrayList<String>();
//		ServiceType service = new ServiceType();
//		service.setTags(tags);
//		
//		ExpressionTree exTree = new ExpressionTree(expression, sdoTree, false);
//		
//		System.out.println("Expression: \""+expression+"\"");
//		System.out.println("Service Tags: "+allTagsAsStr);
//		
//		boolean result = exTree.evaluate(service);
//		
//		System.out.println("evaluation: "+result);
//		
//	}
//	
//	public void test4() throws ParseExpressionException{
//		String expression = "MI:0955";
//		
//		List<String> tagList = new ArrayList<String>();
//		tagList.add("MI:0959");
//		
//		String allTagsAsStr = "";
//		
//		for (String t : tagList){
//			allTagsAsStr = allTagsAsStr + "\""+t+"\" ";
//		}
//		
//		List<String> tags = new ArrayList<String>();
//		ServiceType service = new ServiceType();
//		service.setTags(tags);
//		
//		ExpressionTree exTree = new ExpressionTree(expression, sdoTree, false);
//		
//		System.out.println("Expression: \""+expression+"\"");
//		System.out.println("Service Tags: "+allTagsAsStr);
//		
//		boolean result = exTree.evaluate(service);
//		
//		System.out.println("evaluation: "+result);
//		
//	}
//	
//	public void test5(){
//		
//		String parentId = "MI:0955";
//		String childId = "MI:0959";
//		
//		boolean result = sdoTree.isChildTag(parentId, childId);
//		
//		System.out.println("Result of isChildTag(parentId="+parentId+", childId="+childId+") : "+result);
//		
//		
//	}
//	
//	public void test6(){
//		String termId = "MI:0959";
//		String ontologyName = "MI";
//		
//        Map<String, String> retval = new HashMap<String, String>();
//        QueryServiceLocator locator = new QueryServiceLocator();
//        try {
//            Query service = locator.getOntologyQuery();
//            HashMap roots = service.getTermMetadata(termId, ontologyName);
//            if (roots != null){
//                retval.putAll(roots);
//            }
//
//        } catch (ServiceException e) {
//            e.printStackTrace();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//        
//		System.out.println("result of ols.getTermMetadata(termId="+termId+", ontology="+ontologyName+") :");
//		
//		for (Map.Entry<String, String> entry : retval.entrySet()){
//			System.out.println("Key: \""+entry.getKey()+"\" Value: "+entry.getValue());
//		}
//
//        
//	}
//	
//	
//	
//	public void test7(){
//		OLSClient ols = new OLSClient();
//		String termId = "MI:0959";
//		String ontologyName = "MI";
//		
//		Map<String, String>result = ols.getTermMetadata(termId, ontologyName);
//		
//		System.out.println("result of ols.getTermMetadata(termId="+termId+", ontology="+ontologyName+") :");
//		
//		for (Map.Entry<String, String> entry : result.entrySet()){
//			System.out.println("Key: \""+entry.getKey()+"\" Value: "+entry.getValue());
//		}
//		
//		
//	}
//	
//	public void test8() throws ParseExpressionException{
//		String expression = "NOT MI:0955";
//		
//		List<String> tagList = new ArrayList<String>();
//		tagList.add("MI:0959");
//		
//		String allTagsAsStr = "";
//		
//		for (String t : tagList){
//			allTagsAsStr = allTagsAsStr + "\""+t+"\" ";
//		}
//		
//		List<String> tags = new ArrayList<String>();
//	
//		ServiceType service = new ServiceType();
//		service.setTags(tags);
//		
//		ExpressionTree exTree = new ExpressionTree(expression, sdoTree, false);
//		
//		System.out.println("Expression: \""+expression+"\"");
//		System.out.println("Service Tags: "+allTagsAsStr);
//		
//		boolean result = exTree.evaluate(service);
//		
//		System.out.println("evaluation: "+result);
//		
//	}
//
//	public void test9() throws ParseExpressionException{
//		String expression = "NOT NOT MI:0955";
//		
//		List<String> tagList = new ArrayList<String>();
//		tagList.add("MI:0959");
//		
//		String allTagsAsStr = "";
//		
//		for (String t : tagList){
//			allTagsAsStr = allTagsAsStr + "\""+t+"\" ";
//		}
//		
//		List<String> tags = new ArrayList<String>();
//		ServiceType service = new ServiceType();
//		service.setTags(tags);
//		
//		ExpressionTree exTree = new ExpressionTree(expression, sdoTree, false);
//		
//		System.out.println("Expression: \""+expression+"\"");
//		System.out.println("Service Tags: "+allTagsAsStr);
//		
//		boolean result = exTree.evaluate(service);
//		
//		System.out.println("evaluation: "+result);
//		
//	}
//
//	public void test10() throws ParseExpressionException{
//		String expression = "MI:0959, MI:0957";
//		
//		List<String> tagList = new ArrayList<String>();
//		tagList.add("MI:0959");
//		tagList.add("MI:0957");
//		
//		String allTagsAsStr = "";
//		
//		for (String t : tagList){
//			allTagsAsStr = allTagsAsStr + "\""+t+"\" ";
//		}
//		
//		List<String> tags = new ArrayList<String>();
//		
//		ServiceType service = new ServiceType();
//		service.setTags(tags);
//		
//		ExpressionTree exTree = new ExpressionTree(expression, sdoTree, false);
//		
//		System.out.println("Expression: \""+expression+"\"");
//		System.out.println("Service Tags: "+allTagsAsStr);
//		
//		boolean result = exTree.evaluate(service);
//		
//		System.out.println("evaluation: "+result);
//		
//		System.out.println("Tree: ");
//		exTree.printTree();
//		
//	}
//	
////	public void test11(){
////		TermName tNameMethod = new TermName(new SelfDiscoveringOntologyTree("MI"));
////			
////		try {
////		String out	= (String)tNameMethod.exec(null);
////			System.out.print(out);
////		} catch (TemplateModelException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
////	}
//


}
