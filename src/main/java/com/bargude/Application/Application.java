package com.bargude.Application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
// 1. Fixed JPA configuration to look for the correct transaction manager bean
@EnableJpaRepositories(
        basePackages = "com.bargude.Application.repository.jpa"
)
// 2. KEPT: Necessary to scan your Neo4j Graph repositories
@EnableNeo4jRepositories(
        basePackages = "com.bargude.Application.repository.sdn",
        transactionManagerRef = "neo4jTransactionManager"
)
// 3. KEPT: Necessary to explicitly discover your Postgres JPA entity mappings
@EntityScan(basePackages = "com.bargude.Application.entity")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
