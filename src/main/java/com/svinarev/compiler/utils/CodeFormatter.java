package com.svinarev.compiler.utils;

import org.springframework.stereotype.Component;

import com.svinarev.compiler.models.RawCode;
import com.svinarev.compiler.entities.Exercise;

@Component
public class CodeFormatter {

	public RawCode addLimits(RawCode code) {
		String limitedCode = "import limits\n" + code.getCode();
		
		return RawCode.builder()
					.code(limitedCode)
			   .build();
	}
	
	public RawCode prepareSCT(RawCode code, Exercise exercise) {
		String sct = String.format("%s\n", exercise.getPreExerciseCode())
					+ "from pythonwhat.test_exercise import prep_context\n"
					+ "_, ctxt = prep_context()\n"
					+ "globals().update(ctxt)\n"
					+ "\n"
					+ "from pythonwhat.test_exercise import setup_state\n"
					+ String.format("setup_state(stu_code = \"\"\"%s\"\"\", sol_code = \"\"\"%s\"\"\")\n", code.getCode(), exercise.getSolution())
					+ String.format("%s", exercise.getSct());
		
		return RawCode.builder()
					.code(sct)
			   .build();
		
	}
	
}
