import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class keyword_extraction {

	public static void main(String[] args) throws IOException {
		// get list of sentences
		@SuppressWarnings("resource")
		Scanner s = new Scanner(new File("sample.txt")).useDelimiter(System.lineSeparator());
		ArrayList<String> sentences = new ArrayList<String>();
		while (s.hasNext()){
		    sentences.add(s.next());
		}
		s.close();
		
		// declare arrays for the different kinds of keywords
		// explicit
		String[] exception_words = {"insecure", "susceptible", "error", "null",
				"excpetion", "unavailable", "not thread safe", "illegal",
				"inappropriate", "insecure"};
		String[] recommendation_words = {"deprecate", "better to", "best to",
				"recommended", "less desirable", "discourage"};
		String[] alternative_words = {"instead of", "rather than", "otherwise"};
		String[] imperative_words = {"do not"};
		String[] note_words = {"note that", "notably", "caution"};
		// restricted
		String[] conditional_words = {"under the condition", "whether", "if", "when",
				"assume that"};
		String[] temporal_words = {"before", "after"};
		// generic
		String[] affirmative_words = {"must", "should", "have to", "need to"};
		String[] negative_words = {"do not", "be not", "whether"};
		String[] emphasis_words = {"none", "only", "always"};
		
		// call extract for different kinds of keywords
		ArrayList<String> exceptions = extract(sentences, exception_words);
		ArrayList<String> recommendations = extract(sentences, recommendation_words);
		ArrayList<String> alternatives = extract(sentences, alternative_words); 
		ArrayList<String> imperatives = extract(sentences, imperative_words);
		ArrayList<String> notes = extract(sentences, note_words);
		ArrayList<String> conditionals = extract(sentences, conditional_words);
		ArrayList<String> temporals = extract(sentences, temporal_words);
		ArrayList<String> affirmatives = extract(sentences, affirmative_words);
		ArrayList<String> negatives = extract(sentences, negative_words);
		ArrayList<String> emphases = extract(sentences, emphasis_words);
		
		// export array lists somewhere
		FileWriter writer0 = new FileWriter("exceptions.txt"); 
		for(String str: exceptions) {
		  writer0.write(str);
		}
		writer0.close();
		
		FileWriter writer1 = new FileWriter("recommendations.txt"); 
		for(String str: recommendations) {
		  writer1.write(str);
		}
		writer1.close();
		
		FileWriter writer2 = new FileWriter("alternatives.txt"); 
		for(String str: alternatives) {
		  writer2.write(str);
		}
		writer2.close();
		
		FileWriter writer3 = new FileWriter("imperatives.txt"); 
		for(String str: imperatives) {
		  writer3.write(str);
		}
		writer3.close();
		
		FileWriter writer4 = new FileWriter("notes.txt"); 
		for(String str: notes) {
		  writer4.write(str);
		}
		writer4.close();
		
		FileWriter writer5 = new FileWriter("conditionals.txt"); 
		for(String str: conditionals) {
		  writer5.write(str);
		}
		writer5.close();
		
		FileWriter writer6 = new FileWriter("temporals.txt"); 
		for(String str: temporals) {
		  writer6.write(str);
		}
		writer6.close();
		
		FileWriter writer7 = new FileWriter("affirmatives.txt"); 
		for(String str: affirmatives) {
		  writer7.write(str);
		}
		writer7.close();
		
		FileWriter writer8 = new FileWriter("negatives.txt"); 
		for(String str: negatives) {
		  writer8.write(str);
		}
		writer8.close();
		
		FileWriter writer9 = new FileWriter("emphases.txt"); 
		for(String str: emphases) {
		  writer9.write(str);
		}
		writer9.close();
	}
	
	public static ArrayList<String> extract(ArrayList<String> sentences, String[] words) {
		ArrayList<String> extraction_list = new ArrayList<String>();
		for(String sent:sentences) {
			for(String word:words) {
				if(sent.toLowerCase().contains(word)) {
					extraction_list.add(sent);
					break;
				}
			}
		}
		return extraction_list;
	}

}
