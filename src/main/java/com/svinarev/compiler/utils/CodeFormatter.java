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
	
	/** Adding import of the limitation code */
	public RawCode limitsForKernel(String workingDir) {
		String limitedCode = String.format("import os\nos.chdir('%s')\n", workingDir);
		
		return RawCode.builder()
					.code(limitedCode)
			   .build();
	}
	
	/** Adding pre_exercise_code */
	public RawCode addPreExerciseCode(RawCode code, Exercise exercise) {
		String result = String.format("%s", (exercise.getPreExerciseCode() != null || exercise.getPreExerciseCode().length() > 0)? exercise.getPreExerciseCode() + "\n" : "")
						+ String.format("%s\n", code.getCode() != null ? code.getCode(): "");
						
		return RawCode.builder()
					.code(result)
				.build();
	}

	/** Adding expectation code */
	public RawCode addSCT(RawCode code, RawCode preparedCode, Exercise exercise) {
		
		RawCode studentCode = addPreExerciseCode(code, exercise);
		RawCode solutionCode = addPreExerciseCode(RawCode.builder().code(exercise.getSolution()).build(), exercise);
		
		
		String result = String.format("%s\n", preparedCode.getCode() != null ? preparedCode.getCode(): "")
						+ "from tcs_pythonwhat.test_exercise import prep_context\n"
						+ "_, ctxt = prep_context()\n"
						+ "globals().update(ctxt)\n"
						+ "\n"
						+ "from tcs_pythonwhat.test_exercise import setup_state\n"
						+ String.format("setup_state(stu_code = \"\"\"%s\"\"\", sol_code = \"\"\"%s\"\"\")\n", studentCode.getCode(), solutionCode.getCode())
						+ String.format("%s", exercise.getExpectation() != null ? exercise.getExpectation(): "");
		
		return RawCode.builder()
					.code(result)
			   .build();
	}
	
	/** Changing function calls of plotting a graph to save it to the file */
	public RawCode addPlottingGraph(RawCode code, String filePath) {
		
		/* Template for replacing plt.show with plt.savefig */
		String functionCallCode = String.format("plt.savefig('%s')", filePath);
		
		/* Replacing plt.show with plt.savefig */
		String newCode = code.getCode().replace("plt.show()", functionCallCode)
										.replace("matplotlib.pyplot.show", "matplotlib.pyplot.savefig");
				
		return RawCode.builder()
					.code(newCode)
				.build();
	
	}
	
	/** Formatting the code to check the exercise. 
	 * Adding limits, pre_exercise_code and expectations */
	public RawCode toExerciseChecking(RawCode code, Exercise exercise) {
		RawCode exerciseCode;
		
		exerciseCode = addLimits();
		exerciseCode = addPreExerciseCode(exerciseCode, exercise);
		exerciseCode = addSCT(code, exerciseCode, exercise);
		
		return exerciseCode;
		
	}
	
	/** Formatting the code to check the exercise. 
	 * Adding limits, pre_exercise_code and expectations */
	public RawCode toExerciseCheckingWithPlot(RawCode code, Exercise exercise, String filePath) {
		RawCode exerciseCode;
		
		exerciseCode = addLimits();
		exerciseCode = addPreExerciseCode(exerciseCode, exercise);
		exerciseCode = addSCT(code, exerciseCode, exercise);
		exerciseCode = addPlottingGraph(exerciseCode, filePath);
		
		return exerciseCode;
		
	}
	
	/** Formatting the code to execute with preexercise code. 
	 * Adding limits, pre_exercise_code */
	public RawCode toExecutionWithExercise(RawCode code, Exercise exercise) {
		RawCode exerciseCode;
		
		exerciseCode = addPreExerciseCode(code, exercise);
		exerciseCode = addLimits(exerciseCode);
		
		return exerciseCode;
		
	}
	
	/** Formatting the code to execute with preexercise code 
	 * and plotting a graph. Adding limits, pre_exercise_code
	 * and changing function calls of plotting the graph */
	public RawCode toExecutionWithExerciseAndPlot(RawCode code, Exercise exercise, String filePath) {
		RawCode exerciseCode;
		
		exerciseCode = addPreExerciseCode(code, exercise);
		exerciseCode = addLimits(exerciseCode);
		exerciseCode = addPlottingGraph(exerciseCode, filePath);
		
		return exerciseCode;
		
	}
	
	
	public RawCode toExecuteIntKernelWithGraph(RawCode code) {
		
		String curCode = code.getCode();

		curCode = curCode.replaceAll("plt.", "# plt.");
		
		curCode = curCode.replaceAll("\n", "\r");
		curCode = curCode.replaceAll("\r{2,}", "\r");
		
		
		return RawCode.builder()
					.code(curCode)
				.build();
	}
	
	/** Formatting the code to execute. Adding limits */
	public RawCode toExecution(RawCode code) {
		RawCode exerciseCode;
		
		exerciseCode = addLimits(code);
		
		return exerciseCode;
		
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
		String processedText = text.replace('\t', '\n');
		
		return (int) processedText.chars().filter(ch -> (ch == '\n')).count() + 1;
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
	
	public String prepareTraceback(String a, int b) {
		return "";
	}
	
	/** Processing the traceback. 
	 * Replacing the line number with an error in it. */
	public String processTraceback(String traceback, RawCode readyCode, RawCode rawCode, int offset) {
		int initLength = countLines(rawCode.getCode());
		int resultLength = countLines(readyCode.getCode());		
		
		System.out.println(initLength);
		System.out.println(resultLength);
		
		if (initLength > 1) {
			resultLength -= 1;
		}
		
		int lengthDifference = resultLength- initLength;
		
		int beginIndex = traceback.indexOf("line ") + 5;
		if (beginIndex == 4) return traceback;
		
		int endIndex = beginIndex + countNumberLength(traceback.substring(beginIndex));
		
		int lineNumber = Integer.parseInt(traceback.substring(beginIndex, endIndex)) - lengthDifference + offset;
		
		String processedTraceback = String.format("%s %d%s", traceback.substring(0, beginIndex - 1), lineNumber, traceback.substring(endIndex));
		
		return processedTraceback;
	}
}
