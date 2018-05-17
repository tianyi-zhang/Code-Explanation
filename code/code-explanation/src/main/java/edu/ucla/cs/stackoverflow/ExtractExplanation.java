package edu.ucla.cs.stackoverflow;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


@SuppressWarnings("resource")
public class ExtractExplanation {

	public void getPostExplanations(String input) {
		try {		
			
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
				
				String post = scanner.next();
				System.out.println(post);
				ArrayList<String> sentences = getPostSentences(post);
				System.out.println("Printing p tags:");
				for(int i = 0; i < sentences.size(); i++)
					System.out.println(sentences.get(i));
				
				String code = getPostCode(post);
				System.out.println("\nPrinting code:");
				System.out.println(code);
			
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


/*
private ArrayList<String> getPostSentences(String post) {
	//Extract contents of p tags
	Pattern pTagPattern = Pattern.compile("<p>(.*)</p>");
	Matcher pTagMatcher = pTagPattern.matcher(post);

	ArrayList<String> explanation = new ArrayList<String>();
	
	if(post == null || post.length() == 0)
		return explanation;
	
	while(pTagMatcher.find()) {
		String curr = new String(pTagMatcher.group(0));
		curr = curr.replaceAll("<p>", "");
		curr = curr.replaceAll("</p>", "");
		curr = curr.replaceAll("<.*?>", "");
		curr = curr.replaceAll("<(.+)>([^<]+)</\\\\1>", "");
		curr = curr.replaceAll("http:.*? ", "");
		Scanner sentenceScanner = new Scanner(curr).useDelimiter(":|\\n|\\.|\\<");
		while(sentenceScanner.hasNext()) {
			String token = new String();
			token = sentenceScanner.next();	
			int spaceCount = 0;
			for(int i = 0; i < token.length(); i++)
				if(token.charAt(i) == ' ')
					spaceCount++;
			if(spaceCount != 0 && token.length() > 1)
				explanation.add(token.trim());
					
		}
	}
	
	return explanation;
}
*/