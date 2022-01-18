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
	
	public static String parseShellFeedback(String feedback) {
		/* Index of the feedback's begin */
		int begin = feedback.indexOf("In [1]: ") + 8;
		if (begin == 7) return feedback;
		
		String tmp = feedback.substring(begin);
		
		/* Index of useless tail */
		int end = tmp.lastIndexOf(" Do you really want to exit ([y]/n)? \nkeeping kernel alive") - 9;
		/* If the feedback is empty then we return an empty string */
		if (end == -1) end = 0;
		/* The case when the useless tail wasn't found */
		if (end == -9) return tmp;
		
		tmp = tmp.substring(0, end);
		if (tmp.length() > 0 && tmp.charAt(tmp.length() - 1) =='\n') {
			return tmp.substring(0, tmp.length() - 1);
		}
		
		return tmp;
	}
	
}
