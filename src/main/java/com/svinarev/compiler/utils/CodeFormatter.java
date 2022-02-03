package com.svinarev.compiler.utils;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;

import com.svinarev.compiler.models.RawCode;
import com.svinarev.compiler.entities.Exercise;

@Component
public class CodeFormatter {

	/** Adding import of the limitation code to the empty code*/
	public RawCode addLimits() {
		String limitedCode = "import limits\n";
		
		return RawCode.builder()
					.code(limitedCode)
			   .build();
	}
	
	/** Adding import of the limitation code */
	public RawCode addLimits(RawCode code) {
		String limitedCode = "import limits\n"
								+ code.getCode();
		
		return RawCode.builder()
					.code(limitedCode)
			   .build();
	}
	
	/** Adding pre_exercise_code */
	public RawCode addPreExerciseCode(RawCode code, Exercise exercise) {
//		заменить ("%s\n") на exercise.getPreerewfwefwjkeflwekljfef + "\n" if != null
		String result = String.format("%s", (exercise.getPreExerciseCode() != null || exercise.getPreExerciseCode().length() > 0)? exercise.getPreExerciseCode() + "\n" : "")
						+ String.format("%s\n", code.getCode() != null ? code.getCode(): "");
						
		return RawCode.builder()
					.code(result)
				.build();
	}

	/** Adding expectation code */
	public RawCode addSCT(RawCode studentCode, RawCode preparedCode, Exercise exercise) {
		RawCode stu_code = addPreExerciseCode(studentCode, exercise);
		RawCode sol_code = addPreExerciseCode(RawCode.builder().code(exercise.getSolution()).build(), exercise);
		
		
		String result = String.format("%s\n", preparedCode.getCode() != null ? preparedCode.getCode(): "")
						+ "from tcs_pythonwhat.test_exercise import prep_context\n"
						+ "_, ctxt = prep_context()\n"
						+ "globals().update(ctxt)\n"
						+ "\n"
						+ "from tcs_pythonwhat.test_exercise import setup_state\n"
						+ String.format("setup_state(stu_code = \"\"\"%s\"\"\", sol_code = \"\"\"%s\"\"\")\n", stu_code.getCode(), sol_code.getCode())
						+ String.format("%s", exercise.getExpectation() != null ? exercise.getExpectation(): "");
		
		return RawCode.builder()
					.code(result)
			   .build();
	}
	
	/** Processing expectation code for plotting a graph */
	public Map<String, Object> preparePlotGraph(RawCode code, Exercise exercise, String filePath) {
		Map<String, Object> result = new HashMap<>();
		
		String userCode = code.getCode();
		String solution = exercise.getSolution();
		String expectation = exercise.getExpectation();
		
		String functionCallCode = String.format("plt.savefig('%s')", filePath);
		
		String processedUserCode = userCode.replace("plt.show()", functionCallCode);
		String processedSolution = solution.replace("plt.show()", functionCallCode);
		String processedExpectation = expectation.replace("matplotlib.pyplot.show", "matplotlib.pyplot.savefig");
		
		code.setCode(processedUserCode);
		exercise.setSolution(processedSolution);
		exercise.setExpectation(processedExpectation);
		
		result.put("code", code);
		result.put("exercise", exercise);
		
		return result;
		
	}
	
	/** Counting the number of lines in the code */
	public int countLines(String text) {
		if (text == null) return 0;
		return (int) text.chars().filter(ch -> (ch == '\n')).count() + 1;
	}
	
	/** Сounting the length of a sequence of characters of a number in a string */
	public int countNumberLength(String str) {
		int length = 0; 
		
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i))) {
				break;
			}
			length++;
		}
		
		return length;
	}
	
	/** Replacing the line number with an error in the traceback */
	public String prepareTraceback(String traceback, int lengthDifference) {
		int beginIndex = traceback.indexOf("line ") + 5;
		
		if (beginIndex == 4) return traceback;
		
		int endIndex = beginIndex + countNumberLength(traceback.substring(beginIndex));
		int lineNumber = Integer.parseInt(traceback.substring(beginIndex, endIndex)) - lengthDifference;
		
		String processedTraceback = String.format("%s %d%s", traceback.substring(0, beginIndex - 1), lineNumber, traceback.substring(endIndex));
		
		return processedTraceback;
	}
}
