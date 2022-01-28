package com.svinarev.compiler.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.sentry.Sentry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Map;
import java.util.Base64;
import java.time.LocalDateTime;

import com.svinarev.compiler.models.RawCode;
import com.svinarev.compiler.controllers.CompileController;
import com.svinarev.compiler.models.ExecutionResult;
import com.svinarev.compiler.models.DemonExecutionResult;
import com.svinarev.compiler.entities.Exercise;
import com.svinarev.compiler.entities.ExerciseUserPair;
import com.svinarev.compiler.repositories.ExerciseRepository;
import com.svinarev.compiler.repositories.ExerciseUserPairRepository;
import com.svinarev.compiler.repositories.UserRepository;

import com.svinarev.compiler.utils.ExecutionProcessHandler;
import com.svinarev.compiler.utils.FileHandler;
import com.svinarev.compiler.utils.CodeFormatter;
import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Service
@PropertySource(value = "classpath:application.properties")
public class CompilerService {

	@Value(value = "${programs.dest_dir}")
	private String PY_DEST_DIR;
	
	@Value(value = "${images.dest_dir}")
	private String IMG_DEST_DIR;
	
	@Value (value = "${kernels.manager_file}")
	private String KRNL_MANAGER_FILE;
	
	@Autowired
	private ExecutionProcessHandler execHandler;
	
	@Autowired
	private FileHandler fileHandler;
	
	@Autowired
	private ExerciseRepository exerciseRepository;
	
	@Autowired
	private ExerciseUserPairRepository exerciseUserPairRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired 
	private CodeFormatter codeFormatter;
	
	Logger logger = LoggerFactory.getLogger(CompileController.class);
	
	/** Compiling of user code */
	public ExecutionResult compile(RawCode code) {
		/* Generating a file path */
		String path = PY_DEST_DIR + File.separator + FileHandler.getStringID() + ".py";
		logger.debug("The path to the compiled file {}.", path);
		
		ExecutionResult result;
		
		try{
			/* Writing a user code to the file for compilation */
			fileHandler.write(code.getCode(), path);
			result = execHandler.execute("python3 " + path);	
		}
		catch (Exception e) {
			logger.debug(e.toString() + e.fillInStackTrace().getMessage().toString());
			String error = e.getMessage() + ": " + e.fillInStackTrace().getMessage().toString();
			
			Sentry.captureException(e);
			
			result = ExecutionResult.builder()
						.output("")
						.status("error")
						.error(error)
				   .build();

		}
		
		fileHandler.delete(path);
		
		logger.info("Execution result for the file {}: {}.", path, result.toString());
		
		if (result.isLimited()) {
			Sentry.captureMessage(result.toString());
		}
		
		return result;
	}
	
	/** Execution of user code with the addition of limits */
	public ExecutionResult execute(RawCode code) {
		
		/* Addition of the limits for the execution */
		RawCode limitedCode = codeFormatter.addLimits(code);
		
		logger.debug("Limited code: {}", limitedCode.getCode());
		
		/* Compilation of code */
		ExecutionResult execResult = compile(limitedCode);
		
		/* Formatting the traceback */
		int initLength = codeFormatter.countLines(code.getCode());
		int resultLength = codeFormatter.countLines(limitedCode.getCode());
		String proccesedTraceback = codeFormatter.prepareTraceback(execResult.getError(), resultLength - initLength);
		
		if (initLength > 1) {
			resultLength -= 1;
		}
		
		logger.debug("InitLength: {}", initLength);
		logger.debug("ResultLength: {}", resultLength);
		
		execResult.setError(proccesedTraceback);
		logger.info("Processed traceback: {}", proccesedTraceback);
		
		return execResult;
		
	}
	
	/** Execution of user code with the addition of limits and pre_exercise_code */
	public ExecutionResult executeWithExercise(RawCode code, Long exerciseId) {

		/* Getting exercise by id */
		Optional<Exercise> opt = exerciseRepository.findById(exerciseId);
		
		if (opt.isEmpty()) {
			logger.debug("Exercise with id: {} wasn't found", exerciseId);
			return ExecutionResult.builder()
						.status("error")
						.error(String.format("Exercise with id: %s wasn't found", exerciseId))
						.output("")
				   .build();	
		}

		Exercise exercise = opt.get();
		
		RawCode exerciseCode;
		
		/* Formatting the code to execute. Addding limits and pre_exercise_code */
		exerciseCode = codeFormatter.addPreExerciseCode(code, exercise);
		exerciseCode = codeFormatter.addLimits(exerciseCode);
		
		logger.debug("Code: {}", exerciseCode.getCode());
		
		/* Compiling code*/
		ExecutionResult result = compile(exerciseCode);
		
		/* Formatting the traceback */
		int initLength = codeFormatter.countLines(code.getCode());
		int resultLength = codeFormatter.countLines(exerciseCode.getCode());
		
		if (initLength > 1) {
			resultLength -= 1;
		}
		
		logger.info("InitLength: {}", initLength);
		logger.info("ResultLength: {}", resultLength);
		
		String proccesedTraceback = codeFormatter.prepareTraceback(result.getError(), resultLength - initLength);
		
		result.setError(proccesedTraceback);
		logger.info("Processed traceback: {}", proccesedTraceback);
		
		return result;
		
	}
	
	/** Execution of user code with the addition of limits and pre_exercise_code and 
	 * formatting code for plotting a graph */ 
	public ExecutionResult executeWithExerciseAndPlot(RawCode code, Long exerciseId) {
		/* Getting exercise by id */
		Optional<Exercise> opt = exerciseRepository.findById(exerciseId);
		
		if (opt.isEmpty()) {
			logger.debug("Exercise with id: {} wasn't found", exerciseId);
			return ExecutionResult.builder()
						.status("error")
						.error(String.format("Exercise with id: %s wasn't found", exerciseId))
						.output("")
				   .build();	
		}

		Exercise exercise = opt.get();
		
		/* Generating an image path	*/	
		String imgPath = IMG_DEST_DIR + File.separator + FileHandler.getStringID() + ".png";
		logger.debug("The path to the image file {}.", imgPath);
		
		/* Preparation of user code for the plotting a graph */
		Map<String, Object> pair = codeFormatter.preparePlotGraph(code, exercise, imgPath);
		code = (RawCode) pair.get("code");
		exercise = (Exercise) pair.get("exercise");
		
		RawCode exerciseCode;
		
		/* Formatting the code to execute. Addding limits and pre_exercise_code */
		exerciseCode = codeFormatter.addPreExerciseCode(code, exercise);
		exerciseCode = codeFormatter.addLimits(exerciseCode);
		
		logger.info(exerciseCode.getCode());
		
		/* Compiling code*/
		ExecutionResult execResult = compile(exerciseCode);
		
		/* Formatting the traceback */
		int initLength = codeFormatter.countLines(code.getCode());
		int resultLength = codeFormatter.countLines(exerciseCode.getCode());
		
		if (initLength > 1) {
			resultLength -= 1;
		}
		
		logger.info("InitLength: {}", initLength);
		logger.info("ResultLength: {}", resultLength);
		
		String proccesedTraceback = codeFormatter.prepareTraceback(execResult.getError(), resultLength - initLength - 1);
		
		execResult.setError(proccesedTraceback);
		logger.info("Processed traceback: {}", proccesedTraceback);
		
		if (execResult.getStatus().equals("success")) {
		
			try {
				/* Getting a byte array that represents an image */
				File f = new File(imgPath);
				FileInputStream fis = new FileInputStream(f);
				
				byte[] byteArray = new byte[(int) f.length()];
				fis.read(byteArray);
				
				/* Convert a byte array to the Base64 */
				String base64Image = Base64.getEncoder().encodeToString(byteArray);
				
				execResult.setBytePayload(String.format("data:image/png;base64,%s"	, base64Image));
				
				logger.debug("A graph was plotted. ");
			
				f.delete();
				fis.close();
			
			}
			catch (IOException exc) {
			
				logger.debug(exc.toString() + exc.fillInStackTrace().getMessage().toString());
			}
			
		}	
		
		return execResult;
	}
	
	/** Checking user code for compliance with pythonwhat-library expectations */
	public ExecutionResult checkExercise(RawCode code, Long exerciseId, Long userId) {
		/* Getting exercise by id */
		Optional<Exercise> opt = exerciseRepository.findById(exerciseId);
		
		if (opt.isEmpty()) {
			logger.debug("Exercise with id: {} wasn't found", exerciseId);
			return ExecutionResult.builder()
						.status("error")
						.error(String.format("Exercise with id: %s wasn't found", exerciseId))
						.output("")
				   .build();	
		}
		
		/* Validate userId parameter */
		if (!userRepository.existsById(userId)) {
			logger.debug("User with id: {} wasn't found", userId);
			return ExecutionResult.builder()
						.status("error")
						.error(String.format("User with id: %s wasn't found", userId))
						.output("")
				   .build();
		}
		
		Exercise exercise = opt.get();
		
		/* Validate exercise fields */
		if (exercise.getSolution() == null || 
			exercise.getExpectation() == null) {
			
			logger.debug("Exercise with id: {} is invalid.", exerciseId);
			return ExecutionResult.builder()
						.status("error")
						.error(String.format("Exercise with id: %s is invalid.", exerciseId))
						.output("")
				   .build();
		}
				
		/* The main test of the exercise */
		RawCode exerciseCode;
		
		/* Formatting the code to execute. Addding limits, pre_exercise_code and expectations */
		exerciseCode = codeFormatter.addLimits();
		exerciseCode = codeFormatter.addPreExerciseCode(exerciseCode, exercise);
		exerciseCode = codeFormatter.addSCT(code, exerciseCode, exercise);
		
		logger.info("ExerciseCode: {}", exerciseCode.getCode());
		
		/* Compiling code*/		
		ExecutionResult execResult = compile(exerciseCode);
		
		/* Formatting the feedback */
		execResult.setError(ExecutionResult.parseError(execResult.getError()));
		
		int initLength = codeFormatter.countLines(code.getCode());
		int resultLength = codeFormatter.countLines(exercise.getPreExerciseCode()) + initLength;
		
		if (initLength == 1) {
			resultLength -= 1;
		}
		
		logger.info("InitLength: {}", initLength);
		logger.info("ResultLength: {}", resultLength);
		
		String proccesedTraceback = codeFormatter.prepareTraceback(execResult.getError(), resultLength - initLength);
		
		logger.info("Processed traceback: {}", proccesedTraceback);
		execResult.setError(proccesedTraceback);
		
		
		if (execResult.getStatus().equals("success")) {
			/* The code is executed with pre_exercise_code to check for syntax errors */
			RawCode preCode;
			
			/* Formatting the code to execute. Addding limits and pre_exercise_code */
			preCode = codeFormatter.addPreExerciseCode(code, exercise);
			preCode = codeFormatter.addLimits(preCode);
			
			logger.info("Precode: {}", preCode.getCode());
			
			/* Compiling precode*/
			ExecutionResult preResult = compile(preCode);
			
			if (preResult.getStatus().equals("error")) {
				preResult.setError("Ваш код содержит ошибку. Исправьте её и попробуйте снова!");
				return preResult;
			}

			
			/* Entering a data into the database that the exercise was completed */
			ExerciseUserPair exUsPair = ExerciseUserPair.builder()
					    					.userId(userId)
					    					.exerciseId(exerciseId)
					    					.createdAt(LocalDateTime.now())
					    					.updatedAt(LocalDateTime.now())
										.build();
			
			if (!exerciseUserPairRepository.existsByUserIdAndExerciseId(userId, exerciseId)) {
				exerciseUserPairRepository.save(exUsPair);
			}			
		}
		
		return execResult;
		
	}
	
	private void createExercise() {
		Exercise e = Exercise.builder()
						.id(6L)
						.preExerciseCode("import matplotlib.pyplot as plt\n")
						.solution("# Create data array\n"
								+ "arr = [i for i in range(1, 10)]\n"
								+ "\n"
								+ "# Import the matplotlib.pyplot package as plt\n"
								+ "import matplotlib.pyplot as plt\n"
								+ "\n"
								+ "# Plot a graph based on a data array\n"
								+ "plt.plot(arr)\n"
								+ "\n"
								+ "# Show a graph\n"
								+ "plt.show()")
						.expectation("Ex().check_object(\"arr\")\n"
								+ "Ex().has_import(\"matplotlib.pyplot\", same_as = True)\n"
								+ "Ex().check_function(\"matplotlib.pyplot.plot\", missing_msg = \"Did you specify the first argument?\").check_args([\"args\", 0])\n"
								+ "Ex().check_function(\"matplotlib.pyplot.savefig\", missing_msg = \"Did you call plt.show()\")")
						.courseId(1L)
						.updatedAt(LocalDateTime.now())
						.createdAt(LocalDateTime.now())
						.build();
		exerciseRepository.save(e);
	}
	
	/** Checking user code for compliance with pythonwhat-library expectations and formatting
	 * code for plotting a graph */
	public ExecutionResult plotGraph(RawCode code, Long exerciseId, Long userId) {
		/* Getting exercise by id */
		Optional<Exercise> opt = exerciseRepository.findById(exerciseId);
		
		if (opt.isEmpty()) {
			logger.debug("Exercise with id: {} wasn't found", exerciseId);
			return ExecutionResult.builder()
						.status("error")
						.error(String.format("Exercise with id: %s wasn't found", exerciseId))
						.output("")
				   .build();	
		}
		
		if (!userRepository.existsById(userId)) {
			logger.debug("User with id: {} wasn't found", userId);
			return ExecutionResult.builder()
						.status("error")
						.error(String.format("User with id: %s wasn't found", userId))
						.output("")
				   .build();
		}
		
		Exercise exercise = opt.get();

		/* Validate exercise fields */
		if (exercise.getSolution() == null || 
			exercise.getExpectation() == null) {
				
				logger.debug("Exercise with id: {} is invalid.", exerciseId);
				return ExecutionResult.builder()
							.status("error")
							.error(String.format("Exercise with id: %s is invalid.", exerciseId))
							.output("")
					   .build();
			}
		
		/* Generating an image path	*/	
		String imgPath = IMG_DEST_DIR + File.separator + FileHandler.getStringID() + ".png";
		logger.debug("The path to the image file {}.", imgPath);
		
		/* Preparation of user code for the plotting a graph */
		Map<String, Object> pair = codeFormatter.preparePlotGraph(code, exercise, imgPath);
		code = (RawCode) pair.get("code");
		exercise = (Exercise) pair.get("exercise");
		
		
		/* The main test of the exercise */
		RawCode exerciseCode;
		
		/* Formatting the code to execute. Addding limits, pre_exercise_code and expectations */
		exerciseCode = codeFormatter.addLimits();
		exerciseCode = codeFormatter.addPreExerciseCode(exerciseCode, exercise);
		exerciseCode = codeFormatter.addSCT(code, exerciseCode, exercise);
		
		logger.info("ExerciseCode: {}", exerciseCode.getCode());
		
		/* Compiling code*/		
		ExecutionResult execResult = compile(exerciseCode);
		
		/* Formatting the feedback */
		execResult.setError(ExecutionResult.parseError(execResult.getError()));
		
		int initLength = codeFormatter.countLines(code.getCode());
		int resultLength = codeFormatter.countLines(exercise.getPreExerciseCode()) + initLength;
		
		if (initLength == 1) {
			resultLength -= 1;
		}
		
		logger.info("InitLength: {}", initLength);
		logger.info("ResultLength: {}", resultLength);
		
		String proccesedTraceback = codeFormatter.prepareTraceback(execResult.getError(), resultLength - initLength);
		
		logger.info("Processed traceback: {}", proccesedTraceback);
		execResult.setError(proccesedTraceback);
		
		if (execResult.getStatus().equals("success")) {
		
			/* The code is executed with pre_exercise_code to check for syntax errors */
			RawCode preCode;
			
			/* Formatting the code to execute. Addding limits and pre_exercise_code */
			preCode = codeFormatter.addPreExerciseCode(code, exercise);
			preCode = codeFormatter.addLimits(preCode);
			
			logger.info("Precode: {}", preCode.getCode());
			
			/* Compiling code*/
			ExecutionResult preResult = compile(preCode);
			
			if (preResult.getStatus().equals("error")) {
				preResult.setError("Ваш код содержит ошибку. Исправьте её и попробуйте снова!");
				return preResult;
			}
			
			try {
				/* Getting a byte array that represents an image */
				File f = new File(imgPath);
				FileInputStream fis = new FileInputStream(f);
				
				byte[] byteArray = new byte[(int) f.length()];
				fis.read(byteArray);
				
				/* Convert a byte array to the Base64 */
				String base64Image = Base64.getEncoder().encodeToString(byteArray);
				
				execResult.setBytePayload(String.format("data:image/png;base64,%s"	, base64Image));
				
				logger.debug("A graph was plotted. ");
			
				f.delete();
				fis.close();
			
			}
			catch (IOException exc) {
			
				logger.debug(exc.toString() + exc.fillInStackTrace().getMessage().toString());
			}
			
			/* Entering a data into the database that the exercise was completed */
			ExerciseUserPair exUsPair = ExerciseUserPair.builder()
					    					.userId(userId)
					    					.exerciseId(exerciseId)
					    					.createdAt(LocalDateTime.now())
					    					.updatedAt(LocalDateTime.now())
										.build();
			
			if (!exerciseUserPairRepository.existsByUserIdAndExerciseId(userId, exerciseId)) {
				exerciseUserPairRepository.save(exUsPair);
			}
			
		}
		
		return execResult;
	}
	
	/** Initializes the process of the IPython Shell kernel and returns its id*/
	public DemonExecutionResult initializeKernelProcess() {
		/* Generating a file path */
		String path = KRNL_MANAGER_FILE;
		
		DemonExecutionResult result;
		
		try{
			result = execHandler.executeDemon("jupyter kernel");
		}
		catch (Exception e) {
			logger.debug(e.toString() + e.fillInStackTrace().getMessage().toString());
			String error = e.getMessage() + ": " + e.fillInStackTrace().getMessage().toString();
			
			Sentry.captureException(e);
			
			result = DemonExecutionResult.demonExecResBuilder()
						.output("")
						.status("error")
						.error(error)
						.pid(-1L)
				   .build();
		}
		
		logger.info("Result of the kernel starting: {}", result);
		
		result.setOutput(DemonExecutionResult.parseJupyterOutput(result.getOutput()));
		
		return result;
	}
	
	/** Starts the kernel process and executes the pre-exercise code in it */
	public DemonExecutionResult startKernel(Long exerciseId) {
		
		/* Getting exercise by id */
		Optional<Exercise> opt = exerciseRepository.findById(exerciseId);
		
		if (opt.isEmpty()) {
			logger.debug("Exercise with id: {} wasn't found", exerciseId);
			return DemonExecutionResult.demonExecResBuilder()
						.status("error")
						.error(String.format("Exercise with id: %s wasn't found", exerciseId))
						.output("")
				   .build();	
		}

		Exercise exercise = opt.get();
		
		RawCode exerciseCode = RawCode.builder()
									.code(exercise.getPreExerciseCode())
								.build();
		
		/* Initialize the process of the IPython shell kernel */
		DemonExecutionResult kernelResult = initializeKernelProcess();
		
		/* Execute the pre-exercise code in the kernel */
		ExecutionResult execResult = executeInKernel(exerciseCode, kernelResult.getOutput());
		
		if (execResult.getStatus().equals("error")) {
			return DemonExecutionResult.demonExecResBuilder()
						.status("error")
						.error(execResult.getError())
						.build();
		}
		
		return kernelResult;
	}
	
	/** Executes code inside the kernel selected by id */
	public ExecutionResult executeInKernel(RawCode code, String kernelId) {
		String command = String.format("jupyter console --simple-prompt --existing kernel-%s.json", kernelId);
		
		/* Process the code*/
		code.setCode(code.getCode().replaceAll("\n", "\r"));
		code.setCode(code.getCode().replaceAll("\r{2,}", "\r"));
		
		logger.debug("Command: {}", command);
		logger.debug("Code: {}", code);
		
		ExecutionResult result;
		
		try{
			result = execHandler.execute(command, code.getCode());	
		}
		catch (Exception e) {
			logger.debug(e.toString() + e.fillInStackTrace().getMessage().toString());
			String error = e.getMessage() + ": " + e.fillInStackTrace().getMessage().toString();
			
			Sentry.captureException(e);
			
			result = ExecutionResult.builder()
						.output("")
						.status("error")
						.error(error)
				   .build();

		}
		
		logger.debug("Output: {}", result.getOutput());
		
		result.setOutput(result.parseShellFeedback(result.getOutput()));
		
		logger.info("Result of the execution inside the kernel: {}", result);
		
		return result;
		
	}
	
}
