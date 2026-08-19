package com.bargude.Application.controller;


import com.bargude.Application.Exception.UserNodeAlreadyExist;
import com.bargude.Application.Exception.UserNodeNotFoundException;
import com.bargude.Application.dto.RecommendationResult;
import com.bargude.Application.entity.sdn.UserNode;
import com.bargude.Application.service.UserNodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/graph")
public class GraphController {

	private final UserNodeService userNodeService;

	public GraphController(UserNodeService userNodeService){
		this.userNodeService = userNodeService;
	}

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody UserNode userNode){
		try{
			UserNode un = userNodeService.signup(userNode);
		}catch(UserNodeAlreadyExist e){
			return ResponseEntity.badRequest().body("Error: User already Exist!");
		}
		return ResponseEntity.ok().body(userNode);
	}
	
	@PostMapping("/follow")
	public ResponseEntity<?> follow(@RequestParam String follower, @RequestParam String followee){
        try {
            userNodeService.follow(follower, followee);
        } catch (UserNodeNotFoundException e) {
            return ResponseEntity.badRequest().body("Error: UserNode not found!");
        }
        return ResponseEntity.ok(follower + " is now following " + followee);
    }

	@GetMapping("/following")
	public ResponseEntity<?> following(@RequestParam String username){
		try {
			return ResponseEntity.ok(userNodeService.findFollowing(username));
		} catch (UserNodeNotFoundException e) {
			return ResponseEntity.badRequest().body("Error: UserNode not found!");
		}

    }

	@GetMapping("/followers")
	public ResponseEntity<?> followers(@RequestParam(value = "username", required = true) String username){
		try {
			System.out.println(username);
			return ResponseEntity.ok(userNodeService.findFollowers(username));
		} catch (UserNodeNotFoundException e) {
			return ResponseEntity.badRequest().body("Error: UserNode not found!");
		}

	}

	@GetMapping("/mutual")
	public ResponseEntity<?> mutual(){
		try {
			return ResponseEntity.ok(userNodeService.findMutualFollowing());
		} catch (UserNodeNotFoundException e) {
			return ResponseEntity.badRequest().body("Error: UserNode not found!");
		}

	}

	@GetMapping("/recommendations/{username}")
	public ResponseEntity<?> getRecommendations(@PathVariable String username){
		List<RecommendationResult> recommendations = userNodeService.getRecommendations(username);
		return ResponseEntity.ok().body(recommendations);
	}

}
