package com.svinarev.compiler.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.sentry.Sentry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.svinarev.compiler.controllers.CompileController;

import java.io.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.springframework.stereotype.Component;

@Component
public class FileHandler {
	
	Logger logger = LoggerFactory.getLogger(CompileController.class);
	
	public static String getStringID() {
		
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS"); 
		
		String id = sdf.format(date);
		
		return id;
		
	}
	
	public void write(String data, String path) throws IOException {
		File file = new File(path);
		File parentFile = file.getParentFile();
		
		if (!parentFile.exists()) {
			file.mkdirs();
		}
        if (!file.exists()) {
            file.createNewFile();
            FileWriter fw = new FileWriter(file, UTF_8);
            BufferedWriter bfr = new BufferedWriter(fw);

            bfr.write(data);
            bfr.newLine();
            bfr.close();
            
            logger.debug("File {} was successfully written.", path);
        }
        else {
        	logger.debug("File {} already exists.");
        }
	}
	
	public void delete(String path) {
		File file = new File(path);
		
		file.delete();
		logger.debug("File {} was successfully deleted.", path);
	}
	
	public String read(String path) {
		File file = new File(path);
		
		String result = "";
		
		try {
			FileReader fr = new FileReader(file, UTF_8);
		
			BufferedReader bfr = new BufferedReader(fr);
			
			String line = bfr.readLine();
			while (line != null) {
				result += line + "\n";
				line = bfr.readLine();
			}
		}
		catch (IOException e) {
			logger.debug("The exception was handled while reading the file: {}", e.getMessage() + ": " + e.fillInStackTrace().getMessage().toString());
			Sentry.captureException(e);
		}
		
		return result;
	}	
	
}
