package com.svinarev.compiler.utils;

import org.springframework.stereotype.Component;

import com.svinarev.compiler.models.RawCode;

@Component
public class CodeFormatter {

	public RawCode addLimits(RawCode code) {
		String limitedCode = "import limits\n" + code.getCode();
		
		return RawCode.builder()
					.code(limitedCode)
			   .build();
	}
	
	
	
}
