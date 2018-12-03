package edu.ucla.cs.stackoverflow;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class PartialProgramParser {
	public int cutype;
	private int flag = 0;

	public CompilationUnit getCompilationUnit(String code)
			throws Exception {
		ASTParser parser = getASTParser(code);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cutype = 0;
		if (((CompilationUnit) cu).types().isEmpty()) {
			flag = 1;
			cutype = 1;
			// no class header, try to parse
			String s1 = "public class sample{\n" + code + "\n}";
			parser = getASTParser(s1);
			try {
				cu = (CompilationUnit) parser.createAST(null);
			} catch(Exception e) {
				// parse error
				return null;
			}
			
			cu.accept(new ASTVisitor() {
				public boolean visit(MethodDeclaration node) {
					// find the method header
					flag = 2;
					return false;
				}
			});
			
			if (flag == 1) {
				// this code snippet has no method header nor class header
				s1 = "public class sample{\n public void foo(){\n" + code
						+ "\n}\n}";
				cutype = 2;
				parser = getASTParser(s1);
				try {
					cu = (CompilationUnit) parser.createAST(null);
				} catch(Exception e) {
					// parse error
					return null;
				}
				
				if(cu.toString().isEmpty() || cu.getProblems().length > 0) {
					// parse error
					return null;
				}
				
				return cu;
			} else if (flag == 2) {
				// this code snippet has at least one method but has no class header
				if(cu.getProblems().length > 0) {
					// parse error
					return null;
				}
				
				return cu;
			} else {
				// this should not happen
				return null;
			}
		} else {
			// this code snippet has both class header and method header
			cutype = 0;
			parser = getASTParser(code);
			try {
				cu = (CompilationUnit) parser.createAST(null);
			} catch(Exception e) {
				// parse error
				return null;
			}
			
			if(cu.getProblems().length > 0) {
				// parse error
				return null;
			}
			
			return cu;
		}
	}

	private ASTParser getASTParser(String sourceCode) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setStatementsRecovery(false);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(sourceCode.toCharArray());
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
		parser.setCompilerOptions(options);
		return parser;
	}
}
