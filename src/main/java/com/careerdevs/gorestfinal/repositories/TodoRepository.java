package com.careerdevs.gorestfinal.repositories;

import com.careerdevs.gorestfinal.models.Todo;
import org.springframework.data.repository.CrudRepository;

public interface TodoRepository extends CrudRepository<Todo, Long> {
}
