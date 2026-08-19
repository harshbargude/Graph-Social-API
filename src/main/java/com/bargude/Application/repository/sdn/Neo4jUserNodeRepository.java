package com.bargude.Application.repository.sdn;

import com.bargude.Application.dto.RecommendationResult;
import com.bargude.Application.entity.sdn.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface Neo4jUserNodeRepository extends Neo4jRepository<UserNode, String> {

    @Query("MATCH (u:UserNode {username: $username}) RETURN u")
    Optional<UserNode> findByUsername(@Param("username") String username);

    @Query("MERGE (a:UserNode {username: $follower})" +
            "MERGE (b:UserNode {username: $followee})" +
            "MERGE (a)-[:FOLLOWS]->(b)"
    )
    void follow(@Param("follower") String follower,@Param("followee") String followee);

    @Query("MATCH (a:UserNode {username: $username}) -[:FOLLOWS]-> (b:UserNode) RETURN b")
    List<UserNode> findFollowing(@Param("username") String username);

    @Query("MATCH (a:UserNode) -[:FOLLOWS]-> (b:UserNode {username: $username}) RETURN a")
    List<UserNode> findFollowers(@Param("username") String username);

    @Query("MATCH (a:UserNode)-[:FOLLOWS]->(b:UserNode), (b)-[:FOLLOWS]->(a) " +
            "WHERE a.username < b.username " +
            "RETURN a,b")
    List<UserNode> findMutualFollowing();

    @Query("MATCH (u:UserNode {username: $username})-[:FOLLOWS]->(friend:UserNode)-[:FOLLOWS]->(rec:UserNode) " +
            "WHERE rec <> u AND NOT (u)-[:FOLLOWS]->(rec) " +
            "RETURN rec.username AS recommendedUser, count(friend) AS mutualConnections " +
            "ORDER BY mutualConnections DESC"
    )
    List<RecommendationResult> getRecommendations(@Param("username") String username);
}