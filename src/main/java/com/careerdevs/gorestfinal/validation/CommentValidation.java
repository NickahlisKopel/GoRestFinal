package com.careerdevs.gorestfinal.validation;

import com.careerdevs.gorestfinal.models.Comment;
import com.careerdevs.gorestfinal.models.Post;
import com.careerdevs.gorestfinal.models.User;
import com.careerdevs.gorestfinal.repositories.CommentRepository;
import com.careerdevs.gorestfinal.repositories.PostRepository;

import java.util.Optional;

public class CommentValidation {

    public static ValidationError validateComment (Comment comment, CommentRepository commentRepo,PostRepository postRepo, boolean isUpdate){


        ValidationError errors = new ValidationError();

        //Validate data for post
        if (isUpdate) {
            if (comment.getId() == 0) {
                errors.addError("id", "ID can not be left blank");
            } else {
                Optional<Comment> foundComment = commentRepo.findById(comment.getId());
                if (foundComment.isEmpty()) {
                    errors.addError("id", "No user found with the ID: " + comment.getId());
                }
            }
        }

        String commentName = comment.getName();
        String commentEmail = comment.getEmail();
        String commentBody = comment.getBody();
        long commentPostId = comment.getPost_id();


        if (commentBody == null || commentBody.trim().equals("")) {
            errors.addError("body", "Body can not be left blank");
        }

        if (commentPostId == 0) {
            errors.addError("post_id", "Post_ID can not be left blank");
        } else {
            // is the postUserId connected to an existing user.
            Optional<Post> foundPost = postRepo.findById(commentPostId);

            if (foundPost.isEmpty()) {
                errors.addError("post_id", "Post_ID is invalid because there is no post found with the id: " + commentPostId);
            }


        }

        if (commentName == null || commentName.trim().equals("")) {
            errors.addError("name", "Name can not be left blank");
        }

        if (commentEmail == null || commentEmail.trim().equals("")) {
            errors.addError("email", "Email can not be left blank");
        }

        return errors;
    }


}
