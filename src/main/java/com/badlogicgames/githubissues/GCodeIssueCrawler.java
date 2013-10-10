package com.badlogicgames.githubissues;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GCodeIssueCrawler {
	public static class Issue {
		public String id;
		public String status;
		public String url;
		public String title;
		public String creator;
		public String date;
		public String body;
		public List<Comment> comments = new ArrayList<Comment>();
	}
	
	private static class Comment {
		public String creator;
		public String date;
		public String body;
	}
	
	private static final String GCODE_LIST_ISSUES = "issues/list?can=1&q=&num=%d&start=%d";
	
	public static List<Issue> getIssues(String baseUrl) throws IOException {
		List<Issue> issues = new ArrayList<Issue>();
		
		int start = 1;
		int numIssues = 500;
		while(true) {
			String listUrl = baseUrl + String.format(GCODE_LIST_ISSUES, numIssues, start);
			Document list = Jsoup.connect(listUrl).get();
			start += numIssues;
			List<Issue> newIssues = parseIssues(baseUrl, list);
			if(newIssues.size() == 0) break;
			issues.addAll(newIssues);
		}
		return issues;
	}
	
	public static List<Issue> parseIssues(String baseUrl, Document doc) throws IOException {
		List<Issue> issues = new ArrayList<Issue>();
		Elements rows = doc.select("#resultstable tbody tr");
		if(rows.toString().contains("Your search did not generate any results.")) return issues;
		for(int i = 0; i < rows.size(); i++) {
			Elements cells = rows.get(i).select("td");
			Issue issue = new Issue();
			issue.id = cells.get(1).select("a").text().trim();
			issue.status = cells.get(3).select("a").text().trim();
			issue.title = cells.get(8).select("a").text().trim();
			issue.url = baseUrl + String.format("issues/detail?id=%s", issue.id);
			
			getIssueDetails(issue);
			issues.add(issue);
		}
		return issues;
	}
	
	public static void getIssueDetails(Issue issue) throws IOException {
		Document doc = null;
		try {
			doc = Jsoup.connect(issue.url).get();
		} catch(IOException e) {
			issue.creator = "unknown";
			issue.body = "issue deleted by creator";
			issue.date = "unkown";
			return;
		}
		Elements description = doc.select("div.issuedescription");
		issue.date = description.select("span.date").text();
		issue.creator = description.select(".userlink").text();
		issue.body = description.select("pre").text();
		
		Elements comments = doc.select("div.issuecomment");
		for(int i = 0; i < comments.size(); i++) {
			Element comment = comments.get(i);
			Comment c = new Comment();
			c.creator = comment.select(".userlink").text();
			c.date = comment.select("span.date").text();
			c.body = comment.select("pre").text();
			issue.comments.add(c);
		}
		System.out.println("issue " + issue.id);
	}
	
	public static void saveIssues(File outDir, List<Issue> issues) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		for(Issue i: issues) {
			File outFile = new File(outDir, i.id + ".json");
			mapper.writeValue(outFile, i);
		}
	}
	
	public static void main (String[] args) throws IOException {
		String baseUrl = args[0];		
		if(!baseUrl.endsWith("/")) {
			baseUrl += "/";
		}
		
		File outDir = new File(args[1]);
		if(!outDir.exists()) {
			if(!outDir.mkdirs()) throw new RuntimeException("Couldn't create output dir " + outDir.getAbsolutePath());
		}
		
		saveIssues(outDir, getIssues(baseUrl));
	}
}
