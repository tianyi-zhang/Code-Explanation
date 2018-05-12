package edu.ucla.cs.stackoverflow;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;

public class ExtractJavaPost {
	final String url = "jdbc:mysql://localhost:3306/stackoverflow";
	final String username = "root";
	final String password = "5887526";
	final String query = "select * from answers;";
	String table;
	Connection connect = null;
	ResultSet result = null;
	PreparedStatement prep = null;

	public void connect() {
		try {
			// This will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			connect = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			if (result != null)
				result.close();
			if (prep != null)
				prep.close();
			if (connect != null)
				connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void getJavaPosts(String output, int num) {
		if (connect != null) {
			try {
				prep = connect.prepareStatement(query);
				result = prep.executeQuery();
				
				File o = new File(output);
				if(o.exists()) {
					o.delete();
				}
				o.createNewFile();
				
				int count = 0;
				while(result.next() && count < num) {
					// get the metadata
					String id = result.getString("Id");
					String score = result.getString("Score");
					String isAccepted = result.getString("IsAccepted");
					String viewCount = result.getString("ViewCount");
					String tags = result.getString("tags");
					
//					if(!tags.contains("<android>")) {
//						continue;
//					}
					
					// get the post body
					String body = result.getString("Body");
					ArrayList<String> snippets = getCode(body);
					boolean hasOneStatement = false;
					for(String snippet: snippets) {
						snippet = StringEscapeUtils.unescapeHtml4(snippet);
						if(snippet.contains(";")) {
							// make sure this snippet has at least one statement and is not a code element in text
							hasOneStatement = true;
							break;
						}
					}
					
					if(!hasOneStatement) {
						continue;
					}
					
					// write the post to a text file
					String s = "===UCLA===" + System.lineSeparator();
					s += "PostId: " + id + System.lineSeparator();
					s += "Score: " + score + System.lineSeparator();
					s += "Accepted: " + isAccepted + System.lineSeparator();
					s += "ViewCount: " + viewCount + System.lineSeparator();
					s += "Tags: " + tags + System.lineSeparator();
					s += body + System.lineSeparator();
					
					FileUtils.writeStringToFile(o, s, Charset.defaultCharset(), true);
					count++;
				}
			} catch (SQLException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * Extract the snippet in a <code></code> tag from a given post body 
	 * 
	 * @param body
	 * @return
	 */
	private ArrayList<String> getCode(String body) {
		ArrayList<String> codes = new ArrayList<>();
		String start = "<code>", end = "</code>";
		int s = 0;
		while (true) {
			s = body.indexOf(start, s);
			if (s == -1)
				break;
			s += start.length();
			int e = body.indexOf(end, s);
			if (e == -1)
				break;
			codes.add(body.substring(s, e).trim());
			s = e + end.length();
		}
		return codes;
	}
	
	public static void main(String[] args) {
		ExtractJavaPost extractor = new ExtractJavaPost();
		extractor.connect();
		extractor.getJavaPosts("output/first-100-android-posts.txt", 100);
		extractor.close();
	}
}
