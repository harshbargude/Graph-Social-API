package com.bargude.Application.service;

import com.bargude.Application.entity.jpa.OutboxEvent;
import com.bargude.Application.entity.sdn.UserNode;
import com.bargude.Application.event.EventType;
import com.bargude.Application.repository.jpa.JpaOutboxEventRepository;
import com.bargude.Application.repository.sdn.Neo4jUserNodeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class OutboxProcessor {
    private final JpaOutboxEventRepository outboxRepository;
    private final Neo4jUserNodeRepository neo4jRepository;
    private final ObjectMapper objectMapper;

    public OutboxProcessor(JpaOutboxEventRepository jpaOutboxEventRepository, Neo4jUserNodeRepository neo4jRepository, ObjectMapper objectMapper){
        this.neo4jRepository = neo4jRepository;
        this.objectMapper = objectMapper;
        this.outboxRepository = jpaOutboxEventRepository;
    }

    @Scheduled(fixedDelay = 5000)
//    @Transactional(transactionManager = "transactionManager")
    public void processOutboxEvent(){
        List<OutboxEvent> pendingEvents = outboxRepository.findByProcessedFalse();
        for(OutboxEvent event: pendingEvents){
            try {
                JsonNode payload = objectMapper.readTree(event.getPayload());

                EventType event_type = event.getEventType();

                if (event_type == EventType.USER_CREATED) {
                    JsonNode userNode = payload.at("/user");
                    String username = userNode.get("username").asText();
                    String role = userNode.get("role").asText();

                    var existingUser = neo4jRepository.findByUsername(username);

                    UserNode node = new UserNode();
                    node.setUsername(username);
                    node.setRole(role);
                    neo4jRepository.save(node);
                    System.out.println("Successfully synced user to Neo4j: " + username);
                } else if( event_type == EventType.USER_UPDATED) {
                    JsonNode userNode = payload.at("/user");
                    String username = userNode.get("username").asText();
                    String role = userNode.get("role").asText();
                    var existingUser = neo4jRepository.findByUsername(username);

                    UserNode un = existingUser.get();
                    un.setRole(role);
                    neo4jRepository.save(un);
                    System.out.println("Successfully updated user to Neo4j: " + username);
                } else if (event_type == EventType.USER_FOLLOWED) {
                    JsonNode relNode = payload.at("/relationship");
                    String username = relNode.get("followerUsername").asText();
                    var existingUser = neo4jRepository.findByUsername(username);

                    UserNode node = existingUser.get();
                    String follower = relNode.get("followerUsername").asText();
                    String followed = relNode.get("followedUsername").asText();

                    neo4jRepository.follow(follower, followed);
                    System.out.println(follower + " followed " + followed + " successfully synced to Neo4j");
                } else if (event_type == EventType.USER_DELETED) {

                } else {
                    System.out.println("User already exists, skipping Neo4j save:");
                }

                // 3. Mark the event as processed in Postgres for BOTH scenarios!
                event.setProcessed(true);
                outboxRepository.save(event);

            } catch(Exception e) {
                System.err.println("Failed to process Outbox Event ID: " + event.getId());
                e.printStackTrace();
            }
        }
    }
}
