package com.paias.air.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paias.air.model.n4j.projection.CountryShortestPath;
import com.paias.air.model.n4j.projection.GraphProjectionResult;
import com.paias.air.model.n4j.projection.ProjectionListResult;
import com.paias.air.model.n4j.projection.ShortestPath;
import com.paias.air.model.n4j.search.PathsSnapshot;
import com.paias.air.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class PathService {
    public static final String CREATE_PROJECTION_QUERY = FileUtils.readString("neo4j/queries/create_cost_projection.cypher");
    public static final String SHORTEST_PATH_QUERY = FileUtils.readString("neo4j/queries/shortest_path.cypher");
    public static final String ADD_ZERO_COST_QUERY = FileUtils.readString("neo4j/queries/add_zero_cost.cypher");
    public static final String DELETE_PROJECTION = FileUtils.readString("neo4j/queries/delete_projection.cypher");
    public static final String CREATE_PATH_SEARCH_QUERY = FileUtils.readString("neo4j/queries/create_path_search_projection.cypher");
    public static final String K_SHORTEST_PATHS_QUERY = FileUtils.readString("neo4j/queries/find_k_paths.cypher");
    public static final String GET_PROJECTION_LIST = FileUtils.readString("neo4j/queries/get_projection_list.cypher");
    public static final String COUNTRY_COST_GRAPH_PREFIX = "country-cost-graph-";
    public static final String PATH_SEARCH_GRAPH_PREFIX = "path-search-graph-";
    public static final int MAX_PATH_PROJECTION_SIZE = 8;

    private final Neo4jClient neo4jClient;
    private final ObjectMapper objectMapper;
    private final PathsSnapshotRepository pathsSnapshotRepository;

    public PathService(Neo4jClient neo4jClient, ObjectMapper objectMapper, PathsSnapshotRepository pathsSnapshotRepository) {
        this.neo4jClient = neo4jClient;
        this.objectMapper = objectMapper;
        this.pathsSnapshotRepository = pathsSnapshotRepository;
    }

    public GraphProjectionResult projectCountryCostGraph(YearMonth month) {
        GraphProjectionResult result = neo4jClient.query(CREATE_PROJECTION_QUERY)
                .bind(month.toString()).to("month")
                .fetchAs(GraphProjectionResult.class)
                .mappedBy((typeSystem, record) ->
                        new GraphProjectionResult(
                                record.get("graph").asString(),
                                record.get("nodes").asLong(),
                                record.get("rels").asLong()))
                .one()
                .orElseThrow(() -> new RuntimeException("Failed to project graph"));
        log.info("Created a projection {} node count {} rel count {}", result.graph(), result.nodeCount(), result.relationshipCount());
        return result;
    }

    public List<ProjectionListResult> getCountryCostProjections() {
        return getProjectionWithPrefix(COUNTRY_COST_GRAPH_PREFIX);
    }

    public List<ProjectionListResult> getPathSearchProjections() {
        return getProjectionWithPrefix(PATH_SEARCH_GRAPH_PREFIX);
    }

    private List<ProjectionListResult> getProjectionWithPrefix(String prefix) {
        return neo4jClient.query(GET_PROJECTION_LIST)
                .fetchAs(ProjectionListResult.class)
                .mappedBy((typeSystem, record) ->
                        new ProjectionListResult(
                                record.get("graphName").asString(),
                                record.get("nodeCount").asLong(),
                                record.get("relationshipCount").asLong(),
                                record.get("creationTime").asOffsetDateTime(),
                                record.get("sizeInBytes").asLong()
                        ))
                .all()
                .stream()
                .filter(name -> name.graphName().startsWith(prefix))
                .toList();
    }

    public List<ShortestPath> findShortestPathsFromTo(String sourceId, String destId, YearMonth month) {
        PathsSnapshot snapshot =
                pathsSnapshotRepository.findByFromAndToAndMonth(sourceId, destId, month);
        if (snapshot != null) {
            return deserializePaths(snapshot.getPaths());
        }
        List<ShortestPath> paths = calculateShortestPaths(sourceId, destId, month);
        saveSnapshot(sourceId, destId, month, paths);
        return paths;
    }

    public List<CountryShortestPath> findShortestPathsTo(String destId, YearMonth month) {
        if (!costGraphProjectionExists(month)
        ) {
            projectCountryCostGraph(month);
        }
        neo4jClient.query(ADD_ZERO_COST_QUERY)
                .bind(destId).to("destId")
                .bind(month.toString()).to("month")
                .run();

        return new ArrayList<>(neo4jClient.query(SHORTEST_PATH_QUERY)
                .bind(destId).to("destId")
                .bind(month.toString()).to("month")
                .fetchAs(CountryShortestPath.class)
                .mappedBy((typeSystem, record) ->
                        new CountryShortestPath(
                                record.get("from").asString(),
                                record.get("to").asString(),
                                record.get("totalCost").asDouble(),
                                record.get("totalRawCost").asDouble(),
                                record.get("route").asList(Value::asString)))
                .all());

    }

    public GraphProjectionResult projectPathSearch(String destId, YearMonth month) {
        cleanOldPathProjections();
        findShortestPathsTo(destId, month);
        GraphProjectionResult result = neo4jClient.query(CREATE_PATH_SEARCH_QUERY)
                .bind(destId).to("destId")
                .bind(month.toString()).to("month")
                .fetchAs(GraphProjectionResult.class)
                .mappedBy((typeSystem, record) ->
                        new GraphProjectionResult(
                                record.get("graph").asString(),
                                record.get("nodes").asLong(),
                                record.get("rels").asLong()))
                .one()
                .orElseThrow(() -> new RuntimeException("Failed to project graph"));
        log.info("Created a projection {} node count {} rel count {}", result.graph(), result.nodeCount(), result.relationshipCount());
        return result;
    }

    public List<ShortestPath> calculateShortestPaths(String sourceId, String destId, YearMonth month) {
        Set<String> projections = getPathSearchProjections().stream()
                .map(ProjectionListResult::graphName)
                .collect(Collectors.toSet());
        Arrays.asList(sourceId, destId)
                .forEach(country -> {
                    if (!projections.contains(getPathSearchGraphName(month, country))) {
                        projectPathSearch(country, month);
                    }
                });

        return new ArrayList<>(neo4jClient.query(K_SHORTEST_PATHS_QUERY)
                .bind(sourceId).to("sourceId")
                .bind(destId).to("destId")
                .bind(month.toString()).to("month")
                .fetchAs(ShortestPath.class)
                .mappedBy((typeSystem, record) ->
                        new ShortestPath(
                                record.get("index").asInt(),
                                record.get("totalCost").asDouble(),
                                record.get("route").asList(Value::asString)))
                .all());
    }

    private static String getPathSearchGraphName(YearMonth month, String country) {
        return PATH_SEARCH_GRAPH_PREFIX + country + "-" + month;
    }

    public void deleteProjection(String name) {
        neo4jClient.query(DELETE_PROJECTION)
                .bind(name).to("name")
                .run();
    }

    private boolean costGraphProjectionExists(YearMonth month) {
        return getCountryCostProjections().stream()
                .map(ProjectionListResult::graphName)
                .anyMatch(name -> name.equals(COUNTRY_COST_GRAPH_PREFIX + month));
    }

    private void cleanOldPathProjections() {
        List<ProjectionListResult> projectionList = getPathSearchProjections();
        if (projectionList.size() > MAX_PATH_PROJECTION_SIZE) {
            Optional<ProjectionListResult> earliestProjection = projectionList
                    .stream()
                    .min(Comparator.comparing(ProjectionListResult::creationTime));
            earliestProjection.ifPresent(p -> {
                deleteProjection(p.graphName());
            });
        }
    }

    private void saveSnapshot(
            String sourceId,
            String destId,
            YearMonth month,
            List<ShortestPath> paths) {
        PathsSnapshot snapshot;
        try {
            snapshot = new PathsSnapshot(sourceId, destId, month, objectMapper.writeValueAsString(paths));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize paths snapshot", e);
        }
        pathsSnapshotRepository.save(snapshot);
    }

    private List<ShortestPath> deserializePaths(String paths) {
        try {
            return objectMapper.readValue(
                    paths,
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, ShortestPath.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to deserialize paths snapshot", e);
        }
    }
}
