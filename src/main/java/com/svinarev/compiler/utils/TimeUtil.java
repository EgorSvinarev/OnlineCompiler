package com.svinarev.compiler.utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtil {

	/** Calculates a difference between to times in minutes */
	public static long minuteDifference(LocalDateTime from, LocalDateTime to) {
		
		Duration duration = Duration.between(from, to);
		
		return duration.toMinutes();
	
	}
	
}
