# 🌐 Social Graph & Recommendation Service

A polyglot backend architecture combining **PostgreSQL** 🐘 and **Neo4j** 🕸️ using the **Transactional Outbox Pattern** to provide reliable social networking features and real-time graph recommendations.

---

## 🏛️ System Architecture

* 🐘 **PostgreSQL (System of Record)**: Handles core transactional data (users, credentials, follow relationships) and stores pending outbox events.
* 📦 **Transactional Outbox Pattern**: Ensures safe dual-writes between PostgreSQL and Neo4j without distributed two-phase locking.
* ⚙️ **OutboxProcessor**: Asynchronously polls pending events from PostgreSQL and synchronizes state to the Neo4j graph database.
* 🕸️ **Neo4j (Graph Engine)**: Powers deep relationship traversals, follower queries, and multi-hop "Friend of a Friend" (FOAF) recommendations.

---

## 🛠️ Tech Stack

* ☕ **Java 17+**
* 🍃 **Spring Boot 3** (Spring Data JPA, Spring Data Neo4j)
* 🐘 **PostgreSQL** (Relational database & outbox event store)
* 🕸️ **Neo4j** (Graph database)
* 📦 **Jackson** (JSON event serialization & parsing)

---

## ⚙️ Configuration & Setup

### 1. Database Configuration
Ensure PostgreSQL and Neo4j instances are running, then configure `src/main/resources/application.properties`[cite: 2]:

```properties
server.port=8080

# Neo4j Configuration
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=12345678
spring.data.neo4j.database=ecomb

# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/ecomb_pg
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 2. Build and Run

```bash
# Build the project
./mvnw clean install

# Run the Spring Boot application
./mvnw spring-boot:run
```

---

## 📡 API Endpoints

### 🐘 Relational & Outbox Endpoints (`/api/jpa`)[cite: 2]

| HTTP Method | Endpoint | Description | Payload Example |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/jpa/signup` | Register a new user and create a `USER_CREATED` outbox event | `{"username": "Yash", "password": "password123", "role": "ROLE_USER"}` |
| `POST` | `/api/jpa/update` | Update user details and create a `USER_UPDATED` outbox event | `{"id": 1, "username": "Yash", "password": "newpassword", "role": "ROLE_USER"}` |
| `POST` | `/api/jpa/follow` | Follow a user and create a `USER_FOLLOWED` outbox event | `{"followerUsername": "Yash", "followedUsername": "Harsh"}` |

---

### 🕸️ Graph & Recommendation Endpoints (`/api/graph`)[cite: 2]

| HTTP Method | Endpoint | Description | Query Param / Path |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/graph/following` | Retrieve the list of users a specific user follows | `?username=Yash` |
| `GET` | `/api/graph/followers` | Retrieve all followers of a specific user | `?username=Harsh` |
| `GET` | `/api/graph/mutual` | Retrieve all mutual follow pairs across the graph | *None* |
| `GET` | `/api/graph/recommendations/{username}` | Get 2-hop (FOAF) recommendations ranked by mutual connections | Path variable: `username` |

---

## 🧠 Recommendation Query (Cypher)

Social recommendations are generated using a 2-hop traversal in Neo4j, excluding self-recommendations and users already followed[cite: 2]:

```cypher
MATCH (u:UserNode {username: $username})-[:FOLLOWS]->(friend:UserNode)-[:FOLLOWS]->(rec:UserNode)
WHERE rec <> u AND NOT (u)-[:FOLLOWS]->(rec)
RETURN rec.username AS recommendedUser, count(friend) AS mutualConnections
ORDER BY mutualConnections DESC
```