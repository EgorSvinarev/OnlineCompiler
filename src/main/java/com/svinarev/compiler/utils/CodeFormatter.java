package com.svinarev.compiler.utils;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;

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
					+ "import limits\n"
					+ String.format("setup_state(stu_code = \"\"\"%s\"\"\", sol_code = \"\"\"%s\"\"\")\n", code.getCode(), exercise.getSolution())
					+ String.format("%s", exercise.getExpectation());
		
		return RawCode.builder()
					.code(sct)
			   .build();
	}
	
	public Map<String, Object> preparePlotGraph(RawCode code, Exercise exercise, String filePath) {
		Map<String, Object> result = new HashMap<>();
		
		String userCode = code.getCode();
		String solution = exercise.getSolution();
		String expectation = exercise.getExpectation();
		
		String functionCallCode = String.format("plt.savefig('%s')", filePath);
		
		userCode = userCode.replace("plt.show()", functionCallCode);
		solution = solution.replace("plt.show()", functionCallCode);
		expectation = expectation.replace("matplotlib.pyplot.show", "matplotlib.pyplot.savefig");
		
		code.setCode(userCode);
		exercise.setSolution(solution);
		exercise.setExpectation(expectation);
		
		result.put("code", code);
		result.put("exercise", exercise);
		
		return result;
		
	}
	
}
