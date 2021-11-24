package com.svinarev.compiler.models;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExecutionResult {

	private String status;
	private String output;
	private String error;
	private String bytePayload;
	
	public boolean isLimited() {
		return error.contains("Limitation of execution");
	}
	
	public static String parseError(String stacktrace) {
		int begin = stacktrace.lastIndexOf("protowhat.failure.TestFail: ") + 26;
		
		if (!(stacktrace.contains("Error") || stacktrace.contains("Exception") || stacktrace.contains("TestFail"))) return stacktrace;
		
		if (begin == -1) return stacktrace;
		
		return stacktrace.substring(begin + 2);
	}
	
}
