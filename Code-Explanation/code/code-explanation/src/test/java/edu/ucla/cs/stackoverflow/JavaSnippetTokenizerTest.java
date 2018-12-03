package edu.ucla.cs.stackoverflow;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;

public class JavaSnippetTokenizerTest {
	@Test
	public void test1() throws Exception {
		String snippetPath = "src/test/resources/snippet-151940.txt";
		String snippet = FileUtils.readFileToString(new File(snippetPath), Charset.defaultCharset());
		PartialProgramParser parser = new PartialProgramParser();
		CompilationUnit cu = parser.getCompilationUnit(snippet);
		JavaSnippetTokenizer tokenizer = new JavaSnippetTokenizer();
		cu.accept(tokenizer);
		String groundtruth = "[putInt, Bundle, putString, "
				+ "putDouble, MyBoolean, savedInstanceState, "
				+ "1, myDouble, MyInt, true, Welcome back to Android, "
				+ "onSaveInstanceState, MyString, 1.9, Override, putBoolean]";
		assertEquals(groundtruth, tokenizer.elements.toString());
	}
	
	@Test
	public void test2() throws Exception {
		String snippetPath = "src/test/resources/snippet-46766.txt";
		String snippet = FileUtils.readFileToString(new File(snippetPath), Charset.defaultCharset());
		PartialProgramParser parser = new PartialProgramParser();
		CompilationUnit cu = parser.getCompilationUnit(snippet);
		JavaSnippetTokenizer tokenizer = new JavaSnippetTokenizer();
		cu.accept(tokenizer);
		String groundtruth = "[add, getDrawable, drawable, Drawable, "
				+ "icon, setBounds, getIntrinsicWidth, 0, r, R, "
				+ "getIntrinsicHeight, overlays, defaultMarker, markers, MyItemizedOverlay]";
		assertEquals(groundtruth, tokenizer.elements.toString());
	}
}
