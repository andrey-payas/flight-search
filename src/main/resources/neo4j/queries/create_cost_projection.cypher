WITH 'country-cost-graph-' + $month AS graphName

CALL apoc.do.when(
  gds.graph.exists(graphName),
  'CALL gds.graph.drop($graphName) YIELD graphName AS dropped RETURN dropped',
  'RETURN null AS dropped',
  {graphName: graphName}
) YIELD value

MATCH (from:Country)<-[:FROM]-(route:CountryRoute)-[:TO]->(to:Country)
  WHERE route.price IS NOT NULL
  AND route.month = $month
WITH gds.graph.project(
graphName,
to,
from,
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
