package com.svinarev.compiler.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.svinarev.compiler.models.RawCode;
import com.svinarev.compiler.models.ExecutionResult;

import com.svinarev.compiler.utils.ExecutionProcessHandler;
import com.svinarev.compiler.utils.FileHandler;
import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Value;

@Service
@PropertySource(value = "classpath:application.properties")
public class CompilerService {

	@Value(value = "${programs.dest_dir}")
	private String DEST_DIR;
	
	public ExecutionResult compile(RawCode code) {
		String path = DEST_DIR + File.separator + FileHandler.getStringID() + ".py";
		
		ExecutionResult result;
		
		try{
			FileHandler.write(code.getCode(), path);
			result = ExecutionProcessHandler.execute(path);	
		}
		catch (Exception e) {
			e.printStackTrace();
			String error = e.getMessage() + ": " + e.fillInStackTrace().getMessage().toString();
			
			result = ExecutionResult.builder()
						.output("")
						.status("error")
						.error(error)
				   .build();

		}
		
		FileHandler.deleteFile(path);
		
		return result;
		
		
	}
	
}
