package edu.ucla.cs.stackoverflow;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

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

	private void getPostExplanations(String input, String output) {
		try {		
			// Delimiter string depends on implementation of ExtractJavaPost
			String delimiter = "===UCLA===";
			
			final Boolean DEBUG_FLAG = true;
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
						sentenceComperator c = new sentenceComperator(token);
						snippetOutput += "\nMatches for token: " + token + "\n";
						ArrayList<String> candidates = new ArrayList<String>();
						
						token = "<code>"+ token + "</code>";
						
						for(int k = 0; k < sentences.size(); k++) {
							if(StringUtils.containsIgnoreCase(sentences.get(k), token)) {
								matchCount++;
								// Add sentence to candidates if a substring is found
								// Remove leading space
								candidates.add(sentences.get(k).replaceFirst("^\\s+", ""));
							}
						}
						
						candidates.sort(c);
						
						for(int k = 0; k < candidates.size(); k++) {
							snippetOutput += candidates.get(k) + "|| Score: " 
										 + c.scoreSentence(candidates.get(k), token) + " ||"
										 + "\n";
							
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
	
	private ArrayList<String> getPostSentences(String post) {
		ArrayList<String> explanation = new ArrayList<String>();
		
		if(post == null || post.length() == 0)
			return explanation;
		
		Document postParsed = Jsoup.parse(post);
		Elements pTags = postParsed.select("p");
		
		for (Element element: pTags) {
			String currText = element.html();
			String currArr[] = currText.split(Pattern.quote(". "));
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
	
	private class sentenceComperator implements Comparator<String>{
		
		private String token;
		
		sentenceComperator(String input){
			token = input;
		}
		
		public int compare(String a, String b) {
			int a_score = scoreSentence(a, token);
			int b_score = scoreSentence(b, token);
			
			if(a.length() > b.length())
				a_score++;
			
			if(b.length() > a.length())
				b_score++;
			
			return a_score - b_score;
		}
		
		public int scoreSentence(String sentence, String token) {
	
			int score = 0;
			
			// Check if sentence starts with an uppercase letter
			if(Character.isUpperCase(sentence.charAt(0)))
				score++;
			
			// Check if sentence ends with punctuation
			if(sentence.charAt(sentence.length() - 1) == '.' || sentence.charAt(sentence.length() - 1) == ':'
			|| sentence.charAt(sentence.length() - 1) == '!')
				score++;
			
			int wordCount = sentence.split(" ").length;
			
			// Check if sentence has at least 3 words
			if(wordCount >= 3)
				score++;
			
			// Add a point each time token appears as substring:
			score += StringUtils.countMatches(sentence, token);
				
			// Provide a multiplier if token appears seperate as a "word" (not as substring)
			if(sentence.contains(" " + token + " "))
				score *= 2;
			
			return score;
		}

	}
	
	public static void main(String[] args) {
		ExtractExplanation extractor = new ExtractExplanation();
		extractor.getPostExplanations("output/first-100-posts.txt", "output/first-100-post-processed.txt");
		System.out.printf("Done");
	}
}
