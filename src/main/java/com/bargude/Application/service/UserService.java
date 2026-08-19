package com.bargude.Application.service;

import com.bargude.Application.dto.RecommendationResult;
import com.bargude.Application.entity.jpa.OutboxEvent;
import com.bargude.Application.entity.jpa.User;
import com.bargude.Application.event.EventType;
import com.bargude.Application.repository.jpa.JpaOutboxEventRepository;
import com.bargude.Application.repository.jpa.JpaUserRepository;
import jakarta.validation.Payload;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final JpaUserRepository userRepository;
    private final JpaOutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public UserService(JpaUserRepository userRepository, JpaOutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public User registerUser(String username, String password, String role){
        if(userRepository.findByUsername(username).isPresent()){
            System.out.println("0");
            throw new RuntimeException("User Already Exists!");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role != null ? role : "ROLE_USER");
        User savedUser = userRepository.save(user);
        System.out.println("1");
//        creating OutboxEvent for neo4j sync
         try{
             ObjectNode userNode = objectMapper.createObjectNode();
             userNode.put("username", savedUser.getUsername());
             userNode.put("role", savedUser.getRole());

             ObjectNode rootPayload = objectMapper.createObjectNode();
             rootPayload.set("user", userNode);

             saveOutboxEvent(savedUser.getId(), EventType.USER_CREATED, rootPayload.toString());
         }catch(Exception e){
             throw new RuntimeException("Failed to create Outbox Event!");
         }
         return savedUser;
     }


    @Transactional
    public User updateUser(User user){

        Long id = user.getId();
        String username = user.getUsername();
        String password = user.getPassword();
        String role = user.getRole();

        User userVar = userRepository.findById(id)
                .orElseThrow();


        userVar.setUsername(username);
        userVar.setPassword(password);
        userVar.setRole(role != null ? role : "ROLE_USER");
        User savedUser = userRepository.save(userVar);
        System.out.println("1");
//        creating OutboxEvent for neo4j sync
        try{
            ObjectNode userNode = objectMapper.createObjectNode();
            userNode.put("username", savedUser.getUsername());
            userNode.put("role", savedUser.getRole());

            ObjectNode rootPayload = objectMapper.createObjectNode();
            rootPayload.set("user", userNode);

            saveOutboxEvent(savedUser.getId(), EventType.USER_UPDATED, rootPayload.toString());
        }catch(Exception e){
            throw new RuntimeException("Failed to create Outbox Event!");
        }
        return savedUser;
    }

    @Transactional
    public String followUser(String followerUsername, String followedUsername){
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new RuntimeException("Follower not found!"));
        User following = userRepository.findByUsername(followedUsername)
                .orElseThrow(() -> new RuntimeException("followed user not found!"));

        follower.setFollowing(following);

        User savedUser = userRepository.save(follower);

        try{
            Map<String, Object> payload = new HashMap<>();
//            payload.put("user", savedUser);

            ObjectNode relationshipNode = objectMapper.createObjectNode();
            relationshipNode.put("followerUsername", followerUsername);
            relationshipNode.put("followedUsername", followedUsername);
            payload.put("relationship", relationshipNode);

            String payloadStr = objectMapper.writeValueAsString(payload);

            OutboxEvent event = new OutboxEvent();

            event.setProcessed(false);
            event.setEventType(EventType.USER_FOLLOWED);
            event.setAggregateId(savedUser.getId());
            event.setPayload(payloadStr);
            event.setCreatedAt(LocalDateTime.now());
            outboxEventRepository.save(event);
        }catch (Exception e){
            throw new RuntimeException("Failed to create Outbox Event!");
        }

        return followerUsername + " is now following " + followedUsername;
    }


    private void saveOutboxEvent(Long aggregateId, EventType eventType, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(payload);
        event.setProcessed(false);
        event.setCreatedAt(LocalDateTime.now());
        outboxEventRepository.save(event);
    }
}
