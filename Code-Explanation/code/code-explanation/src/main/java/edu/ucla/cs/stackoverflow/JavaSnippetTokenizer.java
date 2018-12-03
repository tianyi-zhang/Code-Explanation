package edu.ucla.cs.stackoverflow;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;

public class JavaSnippetTokenizer extends ASTVisitor{
	public Set<String> elements;
	
	public JavaSnippetTokenizer() {
		elements = new HashSet<String>();
	}
	
	@Override
	public boolean visit(SimpleName node) {
		String name = node.getIdentifier();
		if(node.getParent() instanceof TypeDeclaration) {
			if(name.equals("sample")) return false; 
		} else if (node.getParent() instanceof MethodDeclaration) {
			if(name.equals("foo")) return false;
		}
		
		elements.add(node.getIdentifier());
		return false;
	}
	
	@Override
	public boolean visit(TypeLiteral node) {
		elements.add(node.getType().toString() + ".class");
		return false;
	}
	
	@Override
	public boolean visit(StringLiteral node) {
		elements.add(node.getLiteralValue());
		return false;
	}
	
	@Override
	public boolean visit(NumberLiteral node) {
		elements.add(node.getToken());
		return false;
	}
	
	@Override
	public boolean visit(BooleanLiteral node) {
		elements.add(new Boolean(node.booleanValue()).toString());
		return false;
	}
}
