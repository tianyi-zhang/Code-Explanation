package edu.ucla.cs.stackoverflow;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;

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
					
					// get the snippet
					String body = result.getString("Body");
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
	
	public static void main(String[] args) {
		ExtractJavaPost extractor = new ExtractJavaPost();
		extractor.connect();
		extractor.getJavaPosts("output/first-100-posts.txt", 100);
		extractor.close();
	}
}
