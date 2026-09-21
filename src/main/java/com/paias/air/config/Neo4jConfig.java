package com.paias.air.config;

import ac.simons.neo4j.migrations.core.Migrations;
import ac.simons.neo4j.migrations.core.MigrationsConfig;
import com.paias.air.model.n4j.heuristics.Route;
import com.paias.air.n4j.YearMonthReadConverter;
import com.paias.air.n4j.YearMonthWriteConverter;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.AbstractNeo4jConfig;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.convert.Neo4jConversions;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Configuration
@EnableNeo4jRepositories("com.paias.air.repository")
@EnableTransactionManagement
public class Neo4jConfig extends AbstractNeo4jConfig {

    @Value("${neo4j.dbname}")
    private String databaseName;
    @Value("${neo4j.port}")
    private String port;
    @Value("${neo4j.url}")
    private String url;
    @Value("${neo4j.username}")
    private String username;
    @Value("${neo4j.password}")
    private String password;

    @Override
    @Bean
    public Driver driver() {
        return GraphDatabase.driver(url +":" + port, AuthTokens.basic(username, password));
    }

    @Override
    protected Collection<String> getMappingBasePackages() {
        return Collections.singletonList("com.paias.air.model");
    }

    @Override
    @Bean
    protected DatabaseSelectionProvider databaseSelectionProvider() {
        return DatabaseSelectionProvider.createStaticDatabaseSelectionProvider(databaseName);
    }

    @Override
    @Bean
    public Neo4jMappingContext neo4jMappingContext(
            Neo4jConversions conversions
    ) {
        Neo4jMappingContext context = new Neo4jMappingContext(conversions);
        context.setInitialEntitySet(Set.of(Route.class));
        context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        context.initialize();
        return context;
    }

    @Override
    @Bean
    public Neo4jConversions neo4jConversions() {
        return new Neo4jConversions(
                List.of(
                        new YearMonthWriteConverter(),
                        new YearMonthReadConverter()
                )
        );
    }


    @Bean(initMethod = "apply")
    public Migrations neo4jMigrations(Driver driver) {
        MigrationsConfig config = MigrationsConfig.builder()
                .withDatabase(databaseName)
                .withLocationsToScan("classpath:neo4j/migrations")
                .build();
        return new Migrations(config, driver);
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

}
