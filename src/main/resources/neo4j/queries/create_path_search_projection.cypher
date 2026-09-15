WITH 'path-search-graph-'+ $destId + '-' + $month AS graphName

CALL apoc.do.when(
  gds.graph.exists(graphName),
  'CALL gds.graph.drop($graphName) YIELD graphName AS dropped RETURN dropped',
  'RETURN null AS dropped',
  {graphName: graphName}
) YIELD value

MATCH (from:Country)<-[:FROM]-(route:CountryRoute)-[:TO]->(to:Country)
  WHERE route.price IS NOT NULL
  AND route.month = $month

MATCH (from)-[:HAS_COST]->(fromCost:Cost {
destination: $destId,
month: $month
})

MATCH (to)-[:HAS_COST]->(toCost:Cost {
destination: $destId,
month: $month
})

WHERE
(fromCost.rawValue * 1.5) >
(toFloat(route.price) + toCost.rawValue)

WITH gds.graph.project(
graphName,
from,
to,
{
  relationshipProperties: {
                            rawCost: toFloat(route.price),
                            cost:    toFloat(route.price) + 10
                          }
}
) AS g
RETURN
  g.graphName          AS graph,
  g.nodeCount          AS nodes,
  g.relationshipCount AS rels
