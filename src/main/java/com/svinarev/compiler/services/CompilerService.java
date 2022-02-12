package com.svinarev.compiler.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import io.sentry.Sentry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Base64;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import com.svinarev.compiler.models.RawCode;
import com.svinarev.compiler.controllers.CompileController;
import com.svinarev.compiler.models.ExecutionResult;
import com.svinarev.compiler.models.DemonExecutionResult;
import com.svinarev.compiler.entities.Exercise;
import com.svinarev.compiler.entities.ExerciseUserPair;
import com.svinarev.compiler.repositories.ExerciseRepository;
import com.svinarev.compiler.repositories.ExerciseUserPairRepository;
import com.svinarev.compiler.repositories.UserRepository;
import com.svinarev.compiler.utils.TimeUtil;

import com.svinarev.compiler.utils.ExecutionProcessHandler;
import com.svinarev.compiler.utils.FileHandler;
import com.svinarev.compiler.utils.CodeFormatter;
import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


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
		RawCode limitedCode = codeFormatter.toExecution(code);
		
		logger.debug("Limited code: {}", limitedCode.getCode());
		
		/* Compilation of code */
		ExecutionResult execResult = compile(limitedCode);
		
		/* Formatting the traceback */
		String traceback = codeFormatter.processTraceback(execResult.getError(), limitedCode, code, 0);
		logger.debug("Traceback: {}", traceback);
		
		execResult.setError(traceback);
		
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
		
		/* Formatting the code for execution */
		RawCode exerciseCode = codeFormatter.toExecutionWithExercise(code, exercise);
		logger.debug("Code: {}", exerciseCode);
		
		/* Compiling code*/
		ExecutionResult execResult = compile(exerciseCode);
		
		/* Processing the traceback */
		String traceback = codeFormatter.processTraceback(execResult.getError(), exerciseCode, code, 0);
		logger.debug("Traceback: {}", traceback);
		
		execResult.setError(traceback);
		
		return execResult;
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
		
		/* Formatting the code for execution */
		RawCode exerciseCode = codeFormatter.toExecutionWithExerciseAndPlot(code, exercise, imgPath);
		logger.debug("Code: {}", exerciseCode);
		
		/* Compiling code*/
		ExecutionResult execResult = compile(exerciseCode);
		
		/* Processing the traceback */
		String traceback = codeFormatter.processTraceback(execResult.getError(), exerciseCode, code, 0);
		logger.debug("Traceback: {}", traceback);
		
		execResult.setError(traceback);
		
		/* Converting an image to base64 */
		if (execResult.getStatus().equals("success")) {
			try {
				String payload = fileHandler.imageToBase64(imgPath);
				execResult.setBytePayload(String.format("data:image/png;base64,%s", payload));
			}
			catch (Exception e) {
				logger.debug(e.toString() + e.fillInStackTrace().getMessage().toString());
				Sentry.captureException(e);
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
		
		/* Formatting the code for checking */
		RawCode exerciseCode = codeFormatter.toExerciseChecking(code, exercise);
		logger.info("ExerciseCode: {}", exerciseCode.getCode());
		
		/* Compiling code*/		
		ExecutionResult execResult = compile(exerciseCode);
		
		/* Formatting the feedback */
		execResult.setError(ExecutionResult.parseError(execResult.getError()));
		
		String traceback = codeFormatter.processTraceback(execResult.getError(), codeFormatter.toExecutionWithExercise(code, exercise), code, 0);
		logger.debug("Traceback: {}", traceback);
		
		execResult.setError(traceback);

		
		if (execResult.getStatus().equals("success")) {
			
			/* The code is executed with pre_exercise_code to check for syntax errors */
			RawCode preCode = codeFormatter.toExecutionWithExercise(exerciseCode, exercise);
			logger.info("Precode: {}", preCode.getCode());
			
			/* Compiling a checking code*/
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
		
		/* Formatting the code for execution */
		RawCode exerciseCode = codeFormatter.toExerciseCheckingWithPlot(code, exercise, imgPath);
		logger.debug("Code: {}", exerciseCode);
		
		/* Compiling code*/
		ExecutionResult execResult = compile(exerciseCode);
		
		/* Formatting the feedback */
		execResult.setError(ExecutionResult.parseError(execResult.getError()));
		
		String traceback = codeFormatter.processTraceback(execResult.getError(), codeFormatter.toExecutionWithExercise(code, exercise), code, 0);
		logger.debug("Traceback: {}", traceback);
		
		execResult.setError(traceback);
		
		if (execResult.getStatus().equals("success")) {
			/* The code is executed with pre_exercise_code to check for syntax errors */
			RawCode preCode = codeFormatter.toExecutionWithExerciseAndPlot(code, exercise, imgPath);
			logger.info("Precode: {}", preCode.getCode());
			
			/* Compiling code*/
			ExecutionResult preResult = compile(preCode);
			
			if (preResult.getStatus().equals("error")) {
				preResult.setError("Ваш код содержит ошибку. Исправьте её и попробуйте снова!");
				return preResult;
			}
			
			try {
				/* Getting a byte array that represents an image */
				String base64Image = fileHandler.imageToBase64(imgPath);
				execResult.setBytePayload(String.format("data:image/png;base64,%s", base64Image));
				logger.debug("A graph was plotted. ");
			
			}
			catch (IOException e) {
				Sentry.captureException(e);
				logger.debug(e.toString() + e.fillInStackTrace().getMessage().toString());
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
		
		/* Initialize the process of the IPython shell kernel */
		DemonExecutionResult kernelResult = initializeKernelProcess();
		
		/* Prepare the pre-exercise code for kernel */
		RawCode exerciseCode = RawCode.builder()
									.code(exercise.getPreExerciseCode())
								.build();
		
		/* Execute the pre-exercise code in the kernel */
		ExecutionResult execResult = executeInKernel(exerciseCode, kernelResult.getOutput());
		
		if (execResult.getStatus().equals("error")) {
			return DemonExecutionResult.demonExecResBuilder()
						.status("error")
						.error(execResult.getError())
						.build();
		}
		
		/* Prepare the limitation code for kernel */
		RawCode limitCode = codeFormatter.limitsForKernel(PY_DEST_DIR);
		
		/* Execute the limitation code in the kernel */
		execResult = executeInKernel(limitCode, kernelResult.getOutput());
		
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
	
	/** Executes code inside the kernel selected by id */
	public ExecutionResult executeInKernelWithPlot(RawCode code, String kernelId) {
		String command = String.format("jupyter console --simple-prompt --existing kernel-%s.json", kernelId);
		logger.debug("Command: {}", command);

		/* Generating an image path	*/	
		String imgPath = IMG_DEST_DIR + File.separator + FileHandler.getStringID() + ".png";
		logger.debug("The path to the image file {}.", imgPath);
		
		/* Formatting the code for execution */
		RawCode processedCode = codeFormatter.addPlottingGraph(code, imgPath);
		logger.debug("Code: {}", processedCode);
		
		/* Process the code*/
		code.setCode(code.getCode().replaceAll("\n", "\r"));
		code.setCode(code.getCode().replaceAll("\r{2,}", "\r"));
		
		ExecutionResult execResult;
		
		try{
			execResult = execHandler.execute(command, code.getCode());	
		}
		catch (Exception e) {
			logger.debug(e.toString() + e.fillInStackTrace().getMessage().toString());
			String error = e.getMessage() + ": " + e.fillInStackTrace().getMessage().toString();
			
			Sentry.captureException(e);
			
			execResult = ExecutionResult.builder()
						.output("")
						.status("error")
						.error(error)
				   .build();

		}
		
		logger.debug("Output: {}", execResult.getOutput());
		execResult.setOutput(execResult.parseShellFeedback(execResult.getOutput()));
		logger.info("Result of the execution inside the kernel: {}", execResult);
		
		/* Converting an image to base64 */
		if (execResult.getStatus().equals("success")) {
			try {
				String payload = fileHandler.imageToBase64(imgPath);
				execResult.setBytePayload(String.format("data:image/png;base64,%s", payload));
			}
			catch (Exception e) {
				logger.debug(e.toString() + e.fillInStackTrace().getMessage().toString());
				Sentry.captureException(e);
			}
		}
		
		
		return execResult;
		
	}
	
	/** Removes all ipython shell cores */
	@Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
	public void cleanOldCores() {
		
		logger.debug("Cleaning of old kernels begins.");
		
		Map<Long, LocalDateTime> timeTable = execHandler.getLivingDemons();
		Map<Long, Process> mapping = execHandler.getMappingTable();
		
		List<Long> keysToDelete = new ArrayList<Long>();
		
		LocalDateTime currentTime = LocalDateTime.now();
		
		for (Long pid: timeTable.keySet()) {
		
			logger.debug("Pid: {}", pid);
			
			LocalDateTime oldTime = timeTable.get(pid);
			Long minuteDifference = TimeUtil.minuteDifference(oldTime, currentTime);
			
			logger.debug("Minute difference: {}", minuteDifference);
			
			if (minuteDifference >= 5) {
				
				Process process = mapping.get(pid);
				process.destroy();
				
				logger.debug("Process with pid: {} created at {} was destroyed.", pid, oldTime);
				
				keysToDelete.add(pid);
				
			}
		}
		
		/* Deleting old cores */
		for (Long pid: keysToDelete) {
			
			timeTable.remove(pid);
			mapping.remove(pid);
		
		}
		
		logger.debug("Cleaning is finished.");
		
	}
	
}
