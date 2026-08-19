package com.bargude.Application.service;

import com.bargude.Application.Exception.UserNodeAlreadyExist;
import com.bargude.Application.Exception.UserNodeNotFoundException;
import com.bargude.Application.dto.RecommendationResult;
import com.bargude.Application.entity.sdn.UserNode;
import com.bargude.Application.repository.sdn.Neo4jUserNodeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserNodeService {

    private final Neo4jUserNodeRepository userNodeRepository;

    public UserNodeService(Neo4jUserNodeRepository userNodeRepository){
        this.userNodeRepository = userNodeRepository;
    }

    public UserNode signup(UserNode userNode) throws UserNodeAlreadyExist {
        if(userNodeRepository.findByUsername(userNode.getUsername()).isPresent()){
            throw new UserNodeAlreadyExist("Username already Exist!");
        }
        try{
            return userNodeRepository.save(userNode);
        }catch (DataIntegrityViolationException e){
            throw new UserNodeAlreadyExist("Username already Exist!");
        }
    }

    public void follow(String follower, String followee) throws UserNodeNotFoundException {
        try{
            UserNode fr = userNodeRepository.findByUsername(follower)
                    .orElseThrow(); //usernot found
            UserNode fe = userNodeRepository.findByUsername(followee)
                    .orElseThrow(); //usernot found

            userNodeRepository.follow(follower, followee);
        }catch(Exception e){
            throw new UserNodeNotFoundException("UserNode not Found!");
        }
    }

    public List<UserNode> findFollowing(String username) throws UserNodeNotFoundException{
        try {
            UserNode user = userNodeRepository.findByUsername(username)
                    .orElseThrow();
        }catch(Exception e){
            throw new UserNodeNotFoundException("UserNode not Found!");
        }


        List<UserNode> list = userNodeRepository.findFollowing(username);
        return list;
    }

    public List<UserNode> findFollowers(String username) throws UserNodeNotFoundException{
        try {
            UserNode user = userNodeRepository.findByUsername(username)
                    .orElseThrow();
        }catch(Exception e){
            throw new UserNodeNotFoundException("UserNode not Found!");
        }
        List<UserNode> list = userNodeRepository.findFollowers(username);

        return userNodeRepository.findFollowers(username);
    }

    public List<UserNode> findMutualFollowing() throws UserNodeNotFoundException{
        List<UserNode> list = userNodeRepository.findMutualFollowing();
        return list;
    }

    public List<RecommendationResult> getRecommendations(String username) {
        return userNodeRepository.getRecommendations(username);
    }

    private UserNode getOrThrow(String username) throws UserNodeNotFoundException {
        return userNodeRepository.findByUsername(username)
                .orElseThrow(() -> new UserNodeNotFoundException("UserNode not Found!"));
    }


}
