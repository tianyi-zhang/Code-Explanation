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
			// Delimiter string depends on implementation of ExtractJavaPost
			String delimiter = "===UCLA===";
			
			final Boolean DEBUG_FLAG = true;
			
			if(output != null) {
				File o = new File(output);
				if(o.exists()) {
					o.delete();
				}
				o.createNewFile();
				
				File d = new File(output+"debug");
				if(d.exists()) {
					d.delete();
				}
				d.createNewFile();
			
			File inputFile = new File(input);
			FileReader reader = new FileReader(inputFile);
			Scanner scanner = new Scanner(reader).useDelimiter(delimiter);
			PartialProgramParser parser = new PartialProgramParser();
			JavaSnippetTokenizer tokenizer = new JavaSnippetTokenizer();
			
			while(scanner.hasNext()) {
				// Use the scanner to read the next post 
				// Extract sentences from the post 
				// Extract code from the post 
				// Tokenize code and match tokens with sentences 
				// Write buffer to output file 
				
				String post = scanner.next();

				ArrayList<String> sentences = getPostSentences(post);

				String code = getPostCode(post);

				
				CompilationUnit cu = parser.getCompilationUnit(code);
				

				if(tokenizer != null && cu != null) {
					cu.accept(tokenizer);
				}
				
				
				for(String snippet : tokenizer.elements) {
					int matchCount = 0;
					String snippetOutput = "\nMatches for token: " + snippet + "\n";
					for(int k = 0; k < sentences.size(); k++) {
						if(sentences.get(k).contains(snippet)) {
							matchCount++;
							snippetOutput += sentences.get(k) + "\n";
						}
					}
					snippetOutput += "Match count: " + matchCount + "\n";
					System.out.println(snippetOutput);
					FileUtils.writeStringToFile(o, snippetOutput, Charset.defaultCharset(), true);
				}
				
				if(DEBUG_FLAG) {
					String postOutput = "Sentences:\n";
					//postOutput += post;
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
					FileUtils.writeStringToFile(d, postOutput, Charset.defaultCharset(), true);
					}
				}
			
			reader.close();
			scanner.close();
			return;
			}
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
	
	public static void main(String[] args) {
		ExtractExplanation extractor = new ExtractExplanation();
		extractor.getPostExplanations("output/first-100-posts.txt", "output/first-100-post-processed.txt");
	}
}
