package com.badlogicgames.githubissues;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;

public class ApiConverter {
	public static void main (String[] args) throws IOException {
		File inputDir = new File(args[0]);
		File outputDir = new File("data");
		if(outputDir.exists()) {
			FileUtils.deleteDirectory(outputDir);
		}
		outputDir.mkdirs();
		
		ObjectMapper mapper = new ObjectMapper();
		
		for(File file: inputDir.listFiles()) {
			
		}
	}
}
