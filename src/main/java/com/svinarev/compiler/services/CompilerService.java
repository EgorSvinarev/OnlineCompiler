package com.svinarev.compiler.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.sentry.Sentry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Map;
import java.util.Base64;
import java.time.LocalDateTime;

import com.svinarev.compiler.models.RawCode;
import com.svinarev.compiler.controllers.CompileController;
import com.svinarev.compiler.models.ExecutionResult;
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
	private String DEST_DIR;
	
	@Value(value = "${images.dest_dir}")
	private String IMG_DEST_DIR;
	
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
	
	public ExecutionResult compile(RawCode code) {
//		Generating a file path
		String path = DEST_DIR + File.separator + FileHandler.getStringID() + ".py";
		logger.debug("The path to the compiled file {}.", path);
		
		ExecutionResult result;
		
		try{
//			Writing a user code to the file for compilation
			fileHandler.write(code.getCode(), path);
			result = execHandler.execute(path);	
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
		
		logger.debug("Execution result for the file {}: {}.", path, result.toString());
		
		if (result.isLimited()) {
			Sentry.captureMessage(result.toString());
		}
		
		return result;
	}
	
	public ExecutionResult execute(RawCode code) {
		
//		Addition of the limits for the execution
		code = codeFormatter.addLimits(code);
		
		logger.info(code.getCode());
		
//		Сode compilation and execution
		ExecutionResult result = compile(code);
		
		return result;
		
	}
	
	public ExecutionResult executeWithExercise(RawCode code, Long exerciseId) {

//		Getting exercise by id
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
		exerciseCode = codeFormatter.addPreExerciseCode(code, exercise);
		exerciseCode = codeFormatter.addLimits(exerciseCode);
		
		logger.info(exerciseCode.getCode());
		
		ExecutionResult result = compile(exerciseCode);
		
		return result;
		
	}
	
	public ExecutionResult executeWithExerciseAndPlot(RawCode code, Long exerciseId) {
//		Getting exercise by id
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
		
//		Generating an image path		
		String imgPath = IMG_DEST_DIR + File.separator + FileHandler.getStringID() + ".png";
		logger.debug("The path to the image file {}.", imgPath);
		
//		Preparation of user code for the plotting a graph
		Map<String, Object> pair = codeFormatter.preparePlotGraph(code, exercise, imgPath);
		code = (RawCode) pair.get("code");
		exercise = (Exercise) pair.get("exercise");
		
		RawCode exerciseCode;
		exerciseCode = codeFormatter.addPreExerciseCode(code, exercise);
		exerciseCode = codeFormatter.addLimits(exerciseCode);
		
		logger.info(exerciseCode.getCode());
		
		ExecutionResult execResult = compile(exerciseCode);
		execResult.setError(ExecutionResult.parseError(execResult.getError()));
		
		if (execResult.getStatus().equals("success")) {
		
			try {
//				Getting a byte array that represents an image
				File f = new File(imgPath);
				FileInputStream fis = new FileInputStream(f);
				
				byte[] byteArray = new byte[(int) f.length()];
				fis.read(byteArray);
				
//				Convert a byte array to the Base64
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
	
	public ExecutionResult checkExercise(RawCode code, Long exerciseId, Long userId) {
//		Getting exercise by id
		Optional<Exercise> opt = exerciseRepository.findById(exerciseId);
		
		if (opt.isEmpty()) {
			logger.debug("Exercise with id: {} wasn't found", exerciseId);
			return ExecutionResult.builder()
						.status("error")
						.error(String.format("Exercise with id: %s wasn't found", exerciseId))
						.output("")
				   .build();	
		}
		
//		Validate userId parameter
		if (!userRepository.existsById(userId)) {
			logger.debug("User with id: {} wasn't found", userId);
			return ExecutionResult.builder()
						.status("error")
						.error(String.format("User with id: %s wasn't found", userId))
						.output("")
				   .build();
		}
		
		Exercise exercise = opt.get();
		
//		Validate exercise fields
		if (exercise.getSolution() == null || 
			exercise.getExpectation() == null) {
			
			logger.debug("Exercise with id: {} is invalid.", exerciseId);
			return ExecutionResult.builder()
						.status("error")
						.error(String.format("Exercise with id: %s is invalid.", exerciseId))
						.output("")
				   .build();
		}
		
		RawCode exerciseCode;
		exerciseCode = codeFormatter.addLimits();
		exerciseCode = codeFormatter.addPreExerciseCode(exerciseCode, exercise);
		exerciseCode = codeFormatter.addSCT(code, exerciseCode, exercise);
		
		logger.info(exerciseCode.getCode());
//		
		ExecutionResult result = compile(exerciseCode);
		result.setError(ExecutionResult.parseError(result.getError()));
		
		if (result.getStatus().equals("success")) {
//			Entering a data into the database that the exercise was completed
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
		
		return result;
		
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
	
	public ExecutionResult plotGraph(RawCode code, Long exerciseId, Long userId) {
//		Getting exercise by id
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

//		Validate exercise fields
		if (exercise.getSolution() == null || 
			exercise.getExpectation() == null) {
				
				logger.debug("Exercise with id: {} is invalid.", exerciseId);
				return ExecutionResult.builder()
							.status("error")
							.error(String.format("Exercise with id: %s is invalid.", exerciseId))
							.output("")
					   .build();
			}
		
//		Generating an image path		
		String imgPath = IMG_DEST_DIR + File.separator + FileHandler.getStringID() + ".png";
		logger.debug("The path to the image file {}.", imgPath);
		
//		Preparation of user code for the plotting a graph
		Map<String, Object> pair = codeFormatter.preparePlotGraph(code, exercise, imgPath);
		code = (RawCode) pair.get("code");
		exercise = (Exercise) pair.get("exercise");
		
		RawCode exerciseCode;
		exerciseCode = codeFormatter.addLimits();
		exerciseCode = codeFormatter.addPreExerciseCode(exerciseCode, exercise);
		exerciseCode = codeFormatter.addSCT(code, exerciseCode, exercise);
		
		ExecutionResult execResult = compile(exerciseCode);
		execResult.setError(ExecutionResult.parseError(execResult.getError()));
		
		if (execResult.getStatus().equals("success")) {
		
			try {
//				Getting a byte array that represents an image
				File f = new File(imgPath);
				FileInputStream fis = new FileInputStream(f);
				
				byte[] byteArray = new byte[(int) f.length()];
				fis.read(byteArray);
				
//				Convert a byte array to the Base64
				String base64Image = Base64.getEncoder().encodeToString(byteArray);
				
				execResult.setBytePayload(String.format("data:image/png;base64,%s"	, base64Image));
				
				logger.debug("A graph was plotted. ");
			
				f.delete();
				fis.close();
			
			}
			catch (IOException exc) {
			
				logger.debug(exc.toString() + exc.fillInStackTrace().getMessage().toString());
			}
			
//			Entering a data into the database that the exercise was completed
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
	
}
