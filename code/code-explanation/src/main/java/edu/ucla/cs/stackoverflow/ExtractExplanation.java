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
import org.apache.commons.lang3.StringEscapeUtils;
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
			final int INFO_LINE_COUNT = 4;
			
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
				
				
				String post = scanner.next();
				String postCodeInfo = "";
				String postCodeRaw = "";
				String postSentences = "";
				String postOutput = "";
				
				
				post  = StringEscapeUtils.unescapeHtml4(post);
				
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
					snippetOutput += "All tokens: " + tokenizer.elements.toString() + "\n";
					
					for(String token : tokenizer.elements) {
						int matchCount = 0;
						snippetOutput += "\nMatches for token: " + token + "\n";
						if(token.length() == 1)
							token = " " + token + " ";
						for(int k = 0; k < sentences.size(); k++) {
							if(sentences.get(k).contains(token)) {
								matchCount++;
								snippetOutput += sentences.get(k) + "\n";
							}
						}
						snippetOutput += "Match count: " + matchCount + "\n";
					}
					postCodeInfo += snippetOutput;
				}
				
					for(int i = 0; i < sentences.size(); i++)
						postSentences += sentences.get(i) + "\n";
					
					postOutput += delimiter + "\n";
					postOutput += post + "\n";
					postOutput += "Sentences\n" + postSentences + "\n";
					postOutput += "Post code:\n" + postCodeRaw + "\n";
					postOutput += "Post code parsed:\n" + postCodeInfo + "\n";
					
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
	
	private ArrayList<String> getPostCode(String post) {
		Pattern codeTagPattern = Pattern.compile("<code>([^<]+)</code>");
		Matcher codeTagMatcher = codeTagPattern.matcher(post);
			
		ArrayList<String> code = new ArrayList<String>();
		
		if(post == null || post.length() == 0)
			return code;
		
		while(codeTagMatcher.find()) {
			code.add(codeTagMatcher.group(0).replaceAll("<code>", "").replaceAll("</code>", ""));
		}
		
		return code;
	}
	
	public static void main(String[] args) {
		ExtractExplanation extractor = new ExtractExplanation();
		extractor.getPostExplanations("output/first-100-posts.txt", "output/first-100-post-processed.txt");
		System.out.printf("Done");
	}
}
