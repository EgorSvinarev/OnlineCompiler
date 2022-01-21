package com.svinarev.compiler.models;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		
		String regex = "In\\s\\[\\d+\\]\\:";
		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		
		/* Сropping the header */
		Matcher matcher = pattern.matcher(feedback);
		
		if (!matcher.find()) {
			return feedback;
		}
		
		int begin = matcher.end() + 1;
		String tmp = feedback.substring(begin);
		
		/* cropping the tail */
		regex = "In\\s\\[\\d+\\]\\:\\sDo\\syou really want to exit \\(\\[y\\]\\/n\\)\\? ";
		pattern = Pattern.compile(regex, Pattern.MULTILINE);
		matcher = pattern.matcher(tmp);
		
		if (!matcher.find()) {
			return tmp;
		}
		
		int end = matcher.start(0);
		end = (end > 1) ? end - 2 : end - 1;
		
		tmp = tmp.substring(0, end);
		
		/* cropping in/out supplements */
		regex = "In\\s\\[\\d+\\]\\:";
		pattern = Pattern.compile(regex, Pattern.MULTILINE);
		matcher = pattern.matcher(tmp);
		
		if (matcher.find()) return tmp.substring(matcher.end() + 1);
		
		regex = "Out\\[\\d+\\]\\:\\s";
		pattern = Pattern.compile(regex, Pattern.MULTILINE);
		matcher = pattern.matcher(tmp);
		
		if (matcher.find()) return tmp.substring(matcher.end());
		
		return tmp;
	}
	
}
