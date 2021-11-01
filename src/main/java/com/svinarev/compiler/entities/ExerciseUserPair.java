package com.svinarev.compiler.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exercises_users")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExerciseUserPair {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exercises_users_id_seq")
	@SequenceGenerator(
		      name = "exercises_users_id_seq",
		      sequenceName = "exercises_users_id_seq",
		      allocationSize = 1)
	private Long id;
	
	@Column(name = "user_id")
	private Long userId;
	
	@Column(name = "exercise_id")
	private Long exerciseId;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
}
