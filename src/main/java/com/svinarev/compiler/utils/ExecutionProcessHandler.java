package com.svinarev.compiler.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.svinarev.compiler.controllers.CompileController;
import com.svinarev.compiler.models.ExecutionResult;

import org.springframework.stereotype.Component;

@Component
public class ExecutionProcessHandler {
	
	Logger logger = LoggerFactory.getLogger(CompileController.class);
	
	public ExecutionResult execute(String path) throws Exception {
		String command = "python3 " + path;
		
		Process process = startProcess(command);
		
		logger.debug("Execution process with a pid {} was successfully started.", process.pid());
		
		String output = readStream(process.getInputStream());
		String error = readStream(process.getErrorStream());
		String status = (error.length() == 0) ? "success" : "error";
		
		process.destroy();
		logger.debug("Execution process with a pid {} was successfully destroyed.", process.pid());
		
		
		return ExecutionResult.builder()
					.status(status)
					.output(output)
					.error(ExecutionResult.parseError(error))
			   .build();
	}
	
	private static Process startProcess(String command) throws Exception {
		Process process = Runtime.getRuntime().exec(command);
		
		return process;
	}
	
	private static String readStream(InputStream stream) throws Exception {
		InputStreamReader inputStreamReader = new InputStreamReader(stream, "GBK");
		
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		
		StringBuilder stringBuilder = new StringBuilder();
		StringBuilder response = new StringBuilder();
		
		String[] splitArray;
		String msg;
		
		while ((msg = bufferedReader.readLine()) != null) {
			
			stringBuilder.append(msg).append("\n");
			
		}
		
		String control = stringBuilder.toString();
		splitArray = control.split("\n");
		
		for (String s : splitArray) {
			response.append(s).append("\n");
		}
		
		bufferedReader.close();
		inputStreamReader.close();
		
		String output = response.toString();
		output = (output == null || output.length() == 0) ? "" : (output.substring(0, output.length() - 1));
				
		return output;

	}
	
}
