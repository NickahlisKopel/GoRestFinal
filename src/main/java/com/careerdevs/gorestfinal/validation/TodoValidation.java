package com.careerdevs.gorestfinal.validation;

import com.careerdevs.gorestfinal.models.Comment;
import com.careerdevs.gorestfinal.models.Post;
import com.careerdevs.gorestfinal.models.Todo;
import com.careerdevs.gorestfinal.models.User;
import com.careerdevs.gorestfinal.repositories.CommentRepository;
import com.careerdevs.gorestfinal.repositories.TodoRepository;
import com.careerdevs.gorestfinal.repositories.UserRepository;

import java.util.Optional;

public class TodoValidation {

    public static ValidationError validateTodo (Todo todo, TodoRepository todoRepo, UserRepository userRepo, boolean isUpdate){


        ValidationError errors = new ValidationError();

        //Validate data for post
        if (isUpdate) {
            if (todo.getId() == 0) {
                errors.addError("id", "ID can not be left blank");
            } else {
                Optional<Todo> foundTodo = todoRepo.findById(todo.getId());
                if (foundTodo.isEmpty()) {
                    errors.addError("id", "No user found with the ID: " + todo.getId());
                }
            }
        }

        String todoTitle = todo.getTitle();
        String todoDueOn = todo.getDue_on();
        long todoUserId = todo.getUser_id();
        String todoStatus = todo.getStatus();

        if (todoTitle == null || todoTitle.trim().equals("")) {
            errors.addError("title", "Title can not be left blank");
        }

        if (todoDueOn == null || todoDueOn.trim().equals("")) {
            errors.addError("body", "Body can not be left blank");
        }

        if (todoUserId == 0) {
            errors.addError("user_id", "User_ID can not be left blank");
        } else {
            // is the postUserId connected to an existing user.
            Optional<User> foundUser = userRepo.findById(todoUserId);

            if (foundUser.isEmpty()) {
                errors.addError("user_id", "User_ID is invalid because there is no user found with the id: " + todoUserId);
            }


        }

        if (todoStatus == null || todoStatus.trim().equals("")){
            errors.addError("Status", "Status can not be left blank");
        }

        return errors;
    }



}
