package com.careerdevs.gorestfinal.validation;

import com.careerdevs.gorestfinal.models.Comment;
import com.careerdevs.gorestfinal.models.Post;
import com.careerdevs.gorestfinal.repositories.CommentRepository;
import com.careerdevs.gorestfinal.repositories.PostRepository;

public class CommentValidation {

    public static ValidationError validateComment (Comment comment, CommentRepository commentRepo, boolean isUpdating){


        ValidationError errors = new ValidationError();

        //Validate data for post

        return errors;
    }


}
