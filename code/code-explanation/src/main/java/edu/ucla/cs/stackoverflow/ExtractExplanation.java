package edu.ucla.cs.stackoverflow;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
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
			
			final Boolean DEBUG_FLAG = false;
			final int INFO_LINE_COUNT = 3;
			
			if(output != null) {
				File o = new File(output);
				if(o.exists()) {
					o.delete();
				}

			
			File inputFile = new File(input);
			FileReader reader = new FileReader(inputFile);
			Scanner scanner = new Scanner(reader).useDelimiter(delimiter);
			PartialProgramParser parser = new PartialProgramParser();

			while(scanner.hasNext()) {
				// Use the scanner to read the next post 
				// Extract sentences from the post 
				// Extract code from the post 
				// Tokenize code and match tokens with sentences 
				// Write buffer to output file 
				
				
				
				String postInfo = "";
				String postCodeInfo = "";
				String postCodeRaw = "";
				String postSentences = "";
				String postOutput = "";
				
				
				if(scanner.hasNextLine())
					scanner.nextLine();
				
				for(int i = 0; i < INFO_LINE_COUNT; i++) {
					if(scanner.hasNextLine())
						postInfo += scanner.nextLine() + '\n';
				}
				
				
				String post = scanner.next();
				post  = StringEscapeUtils.unescapeHtml4(post);
				
				// Advance scanner beyond delimiter string

				
				ArrayList<String> sentences = getPostSentences(post);
				ArrayList<String> code = getPostCode(post);
				
				for(int i = 0; i < code.size(); i++) {
					postCodeRaw += code.get(i) + "\n";
				}

				for(int i = 0; i < code.size(); i++) {
					JavaSnippetTokenizer tokenizer = new JavaSnippetTokenizer();
					CompilationUnit cu = parser.getCompilationUnit(code.get(i));
					String snippetOutput = "";
					if(tokenizer == null || cu == null) {
						snippetOutput = "Tokenizer failed for the snippet: " + code.get(i) + "\n";
						continue;
					}
					cu.accept(tokenizer);
					snippetOutput += "--- Code Snippet " + i + " ---\n";
					snippetOutput += "All tokens: " + tokenizer.elements.toString() + "\n";
					
					for(String token : tokenizer.elements) {
						int matchCount = 0;
						snippetOutput += "\nMatches for token: " + token + "\n";
						
						if(token.length() <= 2)
							token = " " + token + " ";	
						
						for(int k = 0; k < sentences.size(); k++) {
							if(StringUtils.containsIgnoreCase(sentences.get(k), token)) {
								matchCount++;
								snippetOutput += sentences.get(k) + "\n";
							}
						}
						snippetOutput += "Match count: " + matchCount + "\n";
					}
					snippetOutput += "--- --- --- --- --- ---\n";
					postCodeInfo += snippetOutput + "\n";
				}
				
					for(int i = 0; i < sentences.size(); i++)
						postSentences += sentences.get(i) + "\n";
					
					postOutput += delimiter + "\n";
					
					postOutput += postInfo + "\n";
					
					if(DEBUG_FLAG)
						postOutput += post + "\n";
					
					if(DEBUG_FLAG) {
						postOutput += "Sentences\n" + postSentences + "\n";
						postOutput += "Post code:\n" + postCodeRaw + "\n";
					}
					
					postOutput += "Post code parsed:\n" + postCodeInfo + "\n\n";
					
					//System.out.println(postOutput);
					FileUtils.writeStringToFile(o, postOutput, Charset.defaultCharset(), true);
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
	
	private boolean containsIgnoreCase(String string, String token) {
		// TODO Auto-generated method stub
		return false;
	}

	private ArrayList<String> getPostSentences(String post) {
		ArrayList<String> explanation = new ArrayList<String>();
		
		if(post == null || post.length() == 0)
			return explanation;
		
		Document postParsed = Jsoup.parse(post);
		Elements pTags = postParsed.select("p");
		
		for (Element element: pTags) {
			String currText = element.text();
			String currArr[] = currText.split("\\.|\\:|\\n");
			List<String> temp = Arrays.asList(currArr);
			explanation.addAll(temp);
		}
		
		return explanation;
	}
	
	private ArrayList<String> getPostCode(String post) {
		Document postParsed = Jsoup.parse(post);
		Elements codeTags = postParsed.select("code");
			
		ArrayList<String> code = new ArrayList<String>();
		
		if(post == null || post.length() == 0)
			return code;
		
		for (Element element: codeTags) {
			String currText = element.text();
			code.add(currText);
		}
		
		return code;
	}
	
	public static void main(String[] args) {
		ExtractExplanation extractor = new ExtractExplanation();
		extractor.getPostExplanations("output/first-100-posts.txt", "output/first-100-post-processed.txt");
		System.out.printf("Done");
	}
}
