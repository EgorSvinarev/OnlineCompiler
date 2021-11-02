package com.svinarev.compiler.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "exercises")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Exercise {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exercises_id_seq")
	@SequenceGenerator(
		      name = "exercises_id_seq",
		      sequenceName = "exercises_id_seq",
		      allocationSize = 1)
	private Long id;
	
	@Column(name = "exercise_type")
	private int exerciseType;
	
	@Column(name = "xp")
	private int xp;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "hint")
	private String hint;
	
	@Column(name = "video_id")
	private String videoId;
	
	@Column(name = "pre_exercise_code")
	private String preExerciseCode = "";
	
	@Column(name = "sample_code")
	private String sampleCode;
	
	@Column(name = "solution")
	private String solution;
	
	@Column(name = "instruction")
	private String instruction;
	
	@Column(name = "expectation")
	private String expectation;
	
	@Column(name = "course_id")
	private Long courseId;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	@Column(name = "position")
	private int position;
}
