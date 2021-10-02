package com.svinarev.compiler.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;


public class FileHandler {
	
	public static String getStringID() {
		
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS"); 
		
		String id = sdf.format(date);
		
		return id;
		
	}
	
	public static void write(String data, String path) throws IOException {
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
        }
	}
	
	public static void deleteFile(String path) {
		File file = new File(path);
		
		file.delete();
	}
	
}
