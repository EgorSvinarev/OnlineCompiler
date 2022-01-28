package com.svinarev.compiler.utils;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.SequenceInputStream;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.svinarev.compiler.controllers.CompileController;
import com.svinarev.compiler.models.ExecutionResult;
import com.svinarev.compiler.models.DemonExecutionResult;

import org.springframework.stereotype.Component;

@Component
public class ExecutionProcessHandler {
	
	Logger logger = LoggerFactory.getLogger(CompileController.class);
	
	/** Executes the process */
	public ExecutionResult execute(String command) throws Exception {
		
		Process process = startProcess(command);
		
		logger.debug("Execution process with a pid {} was successfully started.", process.pid());
		
		process.waitFor();
		
		logger.debug("Start reading");
		String output = readStream(process.getInputStream());
		logger.debug("Stdout was read");
		String error = readStream(process.getErrorStream());
		logger.debug("Stderr was read");
		String status = (error.length() == 0) ? "success" : "error";
		
		process.destroy();
		logger.debug("Execution process with a pid {} was successfully destroyed.", process.pid());
		
		
		return ExecutionResult.builder()
					.status(status)
					.output(output)
					.error(error)
			   .build();
	}
	
	/** Executes the process with writing data to the STDIN*/
	public ExecutionResult execute(String command, String data) throws Exception {
		
		Process process = startProcess(command);
		
		logger.debug("Execution process with a pid {} was successfully started.", process.pid());
		
		writeStream(process.getOutputStream(), data);
		
		process.waitFor();
		
		logger.debug("Start reading");
		String output = readStream(process.getInputStream());
		logger.debug("Stdout was read");
		String error = readStream(process.getErrorStream());
		logger.debug("Stderr was read");
		String status = (error.length() == 0) ? "success" : "error";
		
		process.destroy();
		logger.debug("Execution process with a pid {} was successfully destroyed.", process.pid());
		
		
		return ExecutionResult.builder()
					.status(status)
					.output(output)
					.error(error)
			   .build();		
	}
	
	/** Executes the demon */
	public DemonExecutionResult executeDemon(String command) throws Exception {
		Process process = startProcess(command);
		
		logger.debug("Execution process with a pid {} was successfully started.", process.pid());
		
		process.waitFor(1, TimeUnit.SECONDS);
		
		logger.debug("Start reading");
		String output = readDemonStream(process.getErrorStream(), 3);
		logger.debug("Stdout was read");
		String error = "";
		logger.debug("Stderr was read");
		String status = (error.length() == 0) ? "success" : "error";
		
		long pid = process.pid();
		
		return DemonExecutionResult.demonExecResBuilder()
				.status(status)
				.output(output)
				.error(error)
				.pid(pid)
		   .build();
	}
	
	/** Starts the execution process in the OS */
	private Process startProcess(String command) throws Exception {
		Process process = Runtime.getRuntime().exec(command);
		
		return process;
	}
	
	public void killProcess(long pid) {
		
	}
	
	/** Reads the output stream from the demon */
	private String readDemonStream(InputStream stdOut, int linesNumber) throws Exception {	
//		logger.debug("{}", stdOut.available());

		BufferedReader reader = new BufferedReader(new InputStreamReader(stdOut, "utf-8"));
		
		StringBuilder stringBuilder = new StringBuilder();
		StringBuilder response = new StringBuilder();
		
		String[] splitArray;
		String msg;
		
		int counter = 0;
		
		while (counter < linesNumber) {
			msg = reader.readLine();
			stringBuilder.append(msg).append("\n");
			counter++;
		}
		
		String control = stringBuilder.toString();
		splitArray = control.split("\n");
		
		for (String s : splitArray) {
			response.append(s).append("\n");
		}
		
		reader.close();
		
		String output = response.toString();
		output = (output == null || output.length() == 0) ? "" : (output.substring(0, output.length() - 1));
				
		return output;
	}	
	
	/** Reads the output stream from the process */
	private String readStream(InputStream stdOut) throws Exception {	
		BufferedReader reader = new BufferedReader(new InputStreamReader(stdOut, "utf-8"));
		
		StringBuilder stringBuilder = new StringBuilder();
		StringBuilder response = new StringBuilder();
		
		String[] splitArray;
		String msg;
		
		while ((msg = reader.readLine()) != null) {
			logger.debug(msg);
			stringBuilder.append(msg).append("\n");
		}
		
		String control = stringBuilder.toString();
		splitArray = control.split("\n");
		
		for (String s : splitArray) {
			response.append(s).append("\n");
		}
		
		reader.close();
		
		String output = response.toString();
		output = (output == null || output.length() == 0) ? "" : (output.substring(0, output.length() - 1));
				
		return output;

	}
	
	public void writeStream(OutputStream stdIn, String data) throws Exception {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdIn));
		
		writer.write(data);
		writer.flush();
		writer.close();
	}
	
}
