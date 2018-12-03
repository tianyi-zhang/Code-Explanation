package edu.ucla.cs.stackoverflow;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;

public class PartialProgramParserTest {
	@Test
	public void testSnippetNotInJava() throws Exception {
		String snippetPath = "src/test/resources/snippet-40189.txt";
		String snippet = FileUtils.readFileToString(new File(snippetPath), Charset.defaultCharset());
		PartialProgramParser parser = new PartialProgramParser();
		CompilationUnit cu = parser.getCompilationUnit(snippet);
		assertEquals(null, cu);
	}
	
	@Test
	public void testSnippetWithStandAloneStatements() throws Exception {
		String snippetPath = "src/test/resources/snippet-46766.txt";
		String snippet = FileUtils.readFileToString(new File(snippetPath), Charset.defaultCharset());
		PartialProgramParser parser = new PartialProgramParser();
		CompilationUnit cu = parser.getCompilationUnit(snippet);
		assertNotEquals(null, cu);
		assertEquals(parser.cutype, 2);
	}
	
	@Test
	public void testSnippetWithSyntaxErrors() throws Exception {
		String snippetPath = "src/test/resources/snippet-121853.txt"; 
		String snippet = FileUtils.readFileToString(new File(snippetPath), Charset.defaultCharset());
		PartialProgramParser parser = new PartialProgramParser();
		CompilationUnit cu = parser.getCompilationUnit(snippet);
		// the ... symbol in the snippet is detected as a syntax error
		assertEquals(null, cu);
	}
	
	@Test
	public void testSnippetWithNoClassDeclaration() throws Exception {
		String snippetPath = "src/test/resources/snippet-151940.txt";
		String snippet = FileUtils.readFileToString(new File(snippetPath), Charset.defaultCharset());
		PartialProgramParser parser = new PartialProgramParser();
		CompilationUnit cu = parser.getCompilationUnit(snippet);
		assertNotEquals(null, cu);
		assertEquals(parser.cutype, 1);
	}
}
