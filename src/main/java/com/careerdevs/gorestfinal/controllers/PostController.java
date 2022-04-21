package com.careerdevs.gorestfinal.controllers;

import com.careerdevs.gorestfinal.models.Post;
import com.careerdevs.gorestfinal.repositories.PostRepository;
import com.careerdevs.gorestfinal.utils.ApiErrorHandling;
import com.careerdevs.gorestfinal.utils.BasicUtils;
import com.careerdevs.gorestfinal.validation.PostValidation;
import com.careerdevs.gorestfinal.validation.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostController {


    @Autowired
    PostRepository postRepository;
    /*

      Required Routes for GoRestSQL Final: complete for each resource; User, Post, Comment, Todo,

             GET route that returns one [resource] by ID from the SQL database
             GET route that returns all [resource]s stored in the SQL database
             DELETE route that deletes one [resource] by ID from SQL database (returns the deleted SQL [resource] data)
             DELETE route that deletes all [resource]s from SQL database (returns how many [resource]s were deleted)
             POST route that queries one [resource] by ID from GoREST and saves their data to your local database (returns
           the SQL [resource] data)
            POST route that uploads all [resource]s from the GoREST API into the SQL database (returns how many
           [resource]s were uploaded)
            POST route that create a [resource] on JUST the SQL database (returns the newly created SQL [resource] data)
            PUT route that updates a [resource] on JUST the SQL database (returns the updated SQL [resource] data)
    * */
//    @GetMapping("/test")
//    public String testRoute () {
//        return "TESTING!";
//    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable ("id") String id) {

        try{
            if(BasicUtils.isStrNaN(id)){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, id + " is not a valid ID");

            }

            long uID = Integer.parseInt(id);

            Optional<Post> foundPost = postRepository.findById(uID);

            if(foundPost.isEmpty()){
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Post not found with Id: " + id);

            }
            return new ResponseEntity<>(foundPost, HttpStatus.OK);


        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        } catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }

    }











    @GetMapping("/all")
    public ResponseEntity<?> getAllPosts (){
        try{
            Iterable<Post> allPosts = postRepository.findAll();

            return new ResponseEntity<>(allPosts, HttpStatus.OK);


        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }

    @PostMapping ("/")
    public ResponseEntity<?> createPost (@RequestBody Post newPost){
        try{
            ValidationError errors = PostValidation.validatePost(newPost, postRepository, false);
            if(errors.hasError()){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, errors.toJSONString());

            }


            Post createdPost = postRepository.save(newPost);

            return new ResponseEntity<> (createdPost, HttpStatus.CREATED);
        }catch (HttpClientErrorException e){
          return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        } catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable ("id") String id) {

        try{
            if(BasicUtils.isStrNaN(id)){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, id + " is not a valid ID");

            }

            long uID = Integer.parseInt(id);

            Optional<Post> foundPost = postRepository.findById(uID);

            if(foundPost.isEmpty()){
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "User not found with Id: " + id);

            }
            postRepository.deleteById(uID);


            return new ResponseEntity<>(foundPost, HttpStatus.OK);


        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        } catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }

    }

    @DeleteMapping("/deleteall")
    public ResponseEntity<?> deleteAllPosts (){
        try{

            long totalPosts = postRepository.count();
            postRepository.deleteAll();

            return new ResponseEntity<>("Posts deleted: " + totalPosts,HttpStatus.OK);

        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        } catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }

    }

    @PostMapping("/upload/{id}")
    public ResponseEntity<?> uploadPostById(
            @PathVariable("id")String postId,
            RestTemplate restTemplate
    ){
        try{

            if(BasicUtils.isStrNaN(postId)){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, postId + " is not a valid ID");

            }
            long uID = Integer.parseInt(postId);
            String url = "https://gorest.co.in/public/v2/posts/" + uID;
            Post foundPost = restTemplate.getForObject(url,Post.class);
            System.out.println(foundPost);

            assert foundPost != null;
            Post savedPost = postRepository.save(foundPost);

            return new ResponseEntity<>(savedPost, HttpStatus.OK);
        } catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        } catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }


    @PostMapping("/uploadall")
    public ResponseEntity<?> uploadAll (
            RestTemplate restTemplate
    ){
        try{
            String url = "https://gorest.co.in/public/v2/posts";

            ResponseEntity<Post[]> response = restTemplate.getForEntity(url,Post[].class);

            Post[] firstPagePosts = response.getBody();

            if(firstPagePosts == null){
                throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to GET first page of posts from GoREST");
            }
            ArrayList<Post> allPosts = new ArrayList<>(Arrays.asList(firstPagePosts));

            HttpHeaders responseHeaders = response.getHeaders();

            String totalPages = Objects.requireNonNull(responseHeaders.get("X-Pagination-Pages")).get(0);
            int totalPgNum = Integer.parseInt(totalPages);

            for(int i = 2; i <= totalPgNum; i++){
                String pageUrl = url + "?page=" + i;
                Post[] pagePosts = restTemplate.getForObject(pageUrl, Post[].class);

                if(pagePosts == null){
                    throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to GET first page " + i + " of posts from GoREST");
                }

                allPosts.addAll(Arrays.asList(firstPagePosts));

            }

            postRepository.saveAll(allPosts);

            return new ResponseEntity<>("Users Created: " + allPosts.size(), HttpStatus.OK);

        }catch(HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(),e.getStatusCode());

        } catch (Exception e){
            return ApiErrorHandling.genericApiError(e);

        }
    }


    @PutMapping("/")
    public ResponseEntity<?> updatePost (@RequestBody Post updatePost){
        try{

            Post savedPost = postRepository.save(updatePost);

            return new ResponseEntity<>(savedPost, HttpStatus.OK);



        }catch(HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(),e.getStatusCode());

        } catch (Exception e){
            return ApiErrorHandling.genericApiError(e);

        }
    }




}
