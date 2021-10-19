package com.svinarev.compiler.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.sentry.Sentry;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.svinarev.compiler.models.RawCode;
import com.svinarev.compiler.controllers.CompileController;
import com.svinarev.compiler.models.ExecutionResult;

import com.svinarev.compiler.utils.ExecutionProcessHandler;
import com.svinarev.compiler.utils.FileHandler;
import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@PropertySource(value = "classpath:application.properties")
public class CompilerService {

	@Value(value = "${programs.dest_dir}")
	private String DEST_DIR;
	
	@Autowired
	private ExecutionProcessHandler execHandler;
	
	@Autowired
	private FileHandler fileHandler;
	
	Logger logger = LoggerFactory.getLogger(CompileController.class);
	
	public ExecutionResult compile(RawCode code) {
		String path = DEST_DIR + File.separator + FileHandler.getStringID() + ".py";
		logger.debug("The path to the compiled file {}.", path);
		
		ExecutionResult result;
		
		try{
			fileHandler.write(code.getCode(), path);
			result = execHandler.execute(path);	
		}
		catch (Exception e) {
			logger.debug(e.toString() + e.fillInStackTrace().getMessage().toString());
			String error = e.getMessage() + ": " + e.fillInStackTrace().getMessage().toString();
			
			Sentry.captureException(e);
			
			result = ExecutionResult.builder()
						.output("")
						.status("error")
						.error(error)
				   .build();

		}
		
		fileHandler.delete(path);
		
		logger.debug("Execution result for the file {}: {}.", path, result.toString());
		
		if (result.isLimited()) {
			Sentry.captureMessage(result.toString());
		}
		
		return result;
		
		
	}
	
}
