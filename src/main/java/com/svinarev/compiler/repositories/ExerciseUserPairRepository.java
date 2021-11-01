package com.svinarev.compiler.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.svinarev.compiler.entities.ExerciseUserPair;

public interface ExerciseUserPairRepository extends JpaRepository<ExerciseUserPair, Long>{

	boolean existsByUserIdAndExerciseId(Long userId, Long exerciseId);
	
}
