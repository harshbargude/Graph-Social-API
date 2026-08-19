package com.bargude.Application.controller;

import com.bargude.Application.dto.FollowRequest;
import com.bargude.Application.entity.jpa.User;
import com.bargude.Application.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jpa")
public class JpaController {
    private final UserService userService;

    public JpaController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user){
        User newUser ;
        try{
            String username = user.getUsername();
            String password = user.getPassword();
            String role = user.getRole();
            User u = userService.registerUser(username, password, role);

            newUser = u;
        }catch(Exception e){
            System.out.println(e);
            return ResponseEntity.badRequest().body("Failed to signup!");
        }

        return ResponseEntity.ok().body(newUser);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody User user){
        User uu ;
        try{
            User updatedUser = userService.updateUser(user);
            uu = updatedUser;
        }catch( Exception e){
            System.out.println(e);
            return ResponseEntity.badRequest().body("Failed To Update!");
        }
        return ResponseEntity.ok().body(uu);
    }

    @PostMapping("/follow")
    public ResponseEntity<?> followUser(@RequestBody FollowRequest followRequest){
        String followerUsername = followRequest.getFollowerUsername();
        String followedUsername = followRequest.getFollowedUsername();
        try{
            userService.followUser(followerUsername, followedUsername);
        }catch (Exception e){
            System.out.println(e);
            return ResponseEntity.badRequest().body("Failed to follow user: " + followedUsername);
        }
        return ResponseEntity.ok().body(followerUsername + " -[followed]-> " + followedUsername);

    }

}
