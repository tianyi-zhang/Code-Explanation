package edu.ucla.cs.stackoverflow;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.Charset;


@SuppressWarnings("resource")
public class ExtractExplanation {

	public void getPostExplanations(String input, String output) {
		try {		
				File o = new File(output);
				if(o.exists()) {
					o.delete();
				}
				o.createNewFile();
			// Delimiter string depends on implementation of ExtractJavaPost
			String delimiter = "===UCLA===";
			
			File inputFile = new File(input);
			FileReader reader = new FileReader(inputFile);
			Scanner scanner = new Scanner(reader).useDelimiter(delimiter);
			
			while(scanner.hasNext()) {
				// Use the scanner to read the next post 
				// Extract sentences from the post 
				// Extract code from the post 
				// Tokenize code and match tokens with sentences 
				// Write buffer to output file 
				
				final Boolean DEBUG_FLAG = true;
				
				String post = scanner.next();

				ArrayList<String> sentences = getPostSentences(post);

				String code = getPostCode(post);

				PartialProgramParser parser = new PartialProgramParser();
				CompilationUnit cu = parser.getCompilationUnit(code);
				JavaSnippetTokenizer tokenizer = new JavaSnippetTokenizer();
				

				if(tokenizer != null && cu != null) {
					cu.accept(tokenizer);
				}
				
				
				if(DEBUG_FLAG) {
					String postOutput = new String();
					postOutput += post;
					postOutput += "Sentences:\n";
					for(int i = 0; i < sentences.size(); i++)
						postOutput += sentences.get(i) + "\n";
					postOutput += "\nCode:\n";
					postOutput += code;
					postOutput += "\nCode tokens:\n";
					if(tokenizer != null && cu != null) {
						postOutput += tokenizer.elements.toString();
					}
					else {
						postOutput += "Java tokenizer failed for this post.\n";
					}
					
					System.out.println(postOutput);
					if(output != null)
						FileUtils.writeStringToFile(o, postOutput, Charset.defaultCharset(), true);
				}
			}
			
			reader.close();
			scanner.close();
			return;
		}
		catch (FileNotFoundException exception) {
			exception.printStackTrace();
		}
		catch (IOException exception) {
			exception.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private ArrayList<String> getPostSentences(String post) {
		ArrayList<String> explanation = new ArrayList<String>();
		
		if(post == null || post.length() == 0)
			return explanation;
		
		Document postParsed = Jsoup.parse(post);
		Elements pTags = postParsed.select("p");
		
		for (Element element: pTags) {
			String currText = element.ownText();
			String currArr[] = currText.split("\\.|\\:|\\n");
			List<String> temp = Arrays.asList(currArr);
			explanation.addAll(temp);
		}
		
		return explanation;
	}
	
	private String getPostCode(String post) {
		Pattern codeTagPattern = Pattern.compile("<code>([^<]+)</code>");
		Matcher codeTagMatcher = codeTagPattern.matcher(post);
			
		String code = new String();
		
		if(post == null || post.length() == 0)
			return code;
		
		while(codeTagMatcher.find()) {
			code += ' ' + codeTagMatcher.group(0);
		}
		
		Pattern removeTags = Pattern.compile("<.+?>");
		Matcher codeTagRemover = removeTags.matcher(code);
		return codeTagRemover.replaceAll("");
	}
}
