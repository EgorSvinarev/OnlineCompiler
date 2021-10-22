package com.svinarev.compiler.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.svinarev.compiler.entities.Exercise;

@Repository
public interface ExerciseRepository extends JpaRepository <Exercise, Long> {
	
	Optional<Exercise> findById(Long id);
	
}
