package com.careerdevs.gorestfinal.controllers;


import com.careerdevs.gorestfinal.models.Post;
import com.careerdevs.gorestfinal.models.Todo;
import com.careerdevs.gorestfinal.models.User;
import com.careerdevs.gorestfinal.repositories.CommentRepository;
import com.careerdevs.gorestfinal.repositories.TodoRepository;
import com.careerdevs.gorestfinal.repositories.UserRepository;
import com.careerdevs.gorestfinal.utils.ApiErrorHandling;
import com.careerdevs.gorestfinal.utils.BasicUtils;
import com.careerdevs.gorestfinal.validation.PostValidation;
import com.careerdevs.gorestfinal.validation.TodoValidation;
import com.careerdevs.gorestfinal.validation.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    TodoRepository todoRepository;
    @Autowired
    UserRepository userRepository;




    @GetMapping("/{id}")
    public ResponseEntity<?> getTodoById(@PathVariable("id") String id) {

        try{
            if(BasicUtils.isStrNaN(id)){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, id + " is not a valid ID");

            }

            long uID = Integer.parseInt(id);

            Optional<Todo> foundTodo = todoRepository.findById(uID);

            if(foundTodo.isEmpty()){
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Todo not found with Id: " + id);

            }
            return new ResponseEntity<>(foundTodo, HttpStatus.OK);


        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        } catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }

    }




    @GetMapping("/all")
    public ResponseEntity<?> getAllTodos (){
        try{
            Iterable<Todo> allTodos = todoRepository.findAll();

            return new ResponseEntity<>(allTodos, HttpStatus.OK);


        }catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }




    @PostMapping("/")
    public ResponseEntity<?> createTodo (@RequestBody Todo newTodo){
        try{
            ValidationError errors = TodoValidation.validateTodo(newTodo, todoRepository,userRepository, false);
            if(errors.hasError()){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, errors.toJSONString());

            }


            Todo createdTodo = todoRepository.save(newTodo);

            return new ResponseEntity<> (createdTodo, HttpStatus.CREATED);
        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        } catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }
    }






    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodoById(@PathVariable ("id") String id) {

        try{
            if(BasicUtils.isStrNaN(id)){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, id + " is not a valid ID");

            }

            long uID = Integer.parseInt(id);

            Optional<Todo> foundTodo = todoRepository.findById(uID);

            if(foundTodo.isEmpty()){
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Todo not found with Id: " + id);

            }
            todoRepository.deleteById(uID);


            return new ResponseEntity<>(foundTodo, HttpStatus.OK);


        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        } catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }

    }




    @DeleteMapping("/deleteall")
    public ResponseEntity<?> deleteAllTodos (){
        try{

            long totalTodos = todoRepository.count();
            todoRepository.deleteAll();

            return new ResponseEntity<>("Todos deleted: " + totalTodos,HttpStatus.OK);

        }catch (HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        } catch (Exception e){
            return ApiErrorHandling.genericApiError(e);
        }

    }



    @PostMapping("/upload/{id}")
    public ResponseEntity<?> uploadTodoById(
            @PathVariable("id")String todoId,
            RestTemplate restTemplate
    ){
        try{

            if(BasicUtils.isStrNaN(todoId)){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, todoId + " is not a valid ID");

            }
            long uID = Integer.parseInt(todoId);
            String url = "https://gorest.co.in/public/v2/todos/" + uID;
            Todo foundTodo = restTemplate.getForObject(url,Todo.class);
            System.out.println(foundTodo);

            if(foundTodo == null){
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Todo with ID: " + todoId + " not found");

            }


            Iterable <User> allUsers = userRepository.findAll();
            List<User> result = new ArrayList<User>();
            allUsers.forEach(result::add);
            long randomId = result.get((int) (result.size() * Math.random())).getId();

            foundTodo.setUser_id(randomId);


            Todo savedTodo = todoRepository.save(foundTodo);

            return new ResponseEntity<>(savedTodo, HttpStatus.OK);
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
            String url = "https://gorest.co.in/public/v2/todos";

            ResponseEntity<Todo[]> response = restTemplate.getForEntity(url,Todo[].class);

            Todo[] firstPageTodos = response.getBody();

            if(firstPageTodos == null){
                throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to GET first page of todos from GoREST");
            }
            ArrayList<Todo> allTodos = new ArrayList<>(Arrays.asList(firstPageTodos));

            HttpHeaders responseHeaders = response.getHeaders();

            String totalPages = Objects.requireNonNull(responseHeaders.get("X-Pagination-Pages")).get(0);
            int totalPgNum = Integer.parseInt(totalPages);

            for(int i = 2; i <= totalPgNum; i++){
                String pageUrl = url + "?page=" + i;
                Post[] pagePosts = restTemplate.getForObject(pageUrl, Post[].class);

                if(pagePosts == null){
                    throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to GET first page " + i + " of todos from GoREST");
                }

                allTodos.addAll(Arrays.asList(firstPageTodos));

            }


            Iterable <User> allUsers = userRepository.findAll();
            List<User> result = new ArrayList<User>();
            allUsers.forEach(result::add);


            for (Todo allTodo : allTodos) {
                long randomId = result.get((int) (result.size() * Math.random())).getId();
                allTodo.setUser_id(randomId);
            }


            todoRepository.saveAll(allTodos);

            return new ResponseEntity<>("Users Created: " + allTodos.size(), HttpStatus.OK);

        }catch(HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(),e.getStatusCode());

        } catch (Exception e){
            return ApiErrorHandling.genericApiError(e);

        }
    }





    @PutMapping("/")
    public ResponseEntity<?> updateTodo (@RequestBody Todo updateTodo){
        try{


            ValidationError errors = TodoValidation.validateTodo(updateTodo, todoRepository,userRepository, true);
            if(errors.hasError()){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, errors.toJSONString());

            }else{

                Todo savedTodo = todoRepository.save(updateTodo);

                return new ResponseEntity<>(savedTodo, HttpStatus.OK);
            }






        }catch(HttpClientErrorException e){
            return ApiErrorHandling.customApiError(e.getMessage(),e.getStatusCode());

        } catch (Exception e){
            return ApiErrorHandling.genericApiError(e);

        }
    }








}
