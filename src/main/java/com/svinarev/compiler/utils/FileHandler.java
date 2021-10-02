package com.svinarev.compiler.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.svinarev.compiler.controllers.CompileController;

import java.io.*;

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
            FileWriter fw = new FileWriter(path);
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
	
	public static void deleteFile(String path) {
		File file = new File(path);
		
		file.delete();
	}
	
}
