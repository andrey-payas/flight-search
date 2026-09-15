MATCH (destination:Country {id: $destId})
CALL gds.allShortestPaths.dijkstra.stream(
'country-cost-graph-'+ $month,
{
  sourceNode: destination,
  relationshipWeightProperty: 'cost'
}
)
YIELD sourceNode, targetNode, totalCost, path
WITH
  destination,
  totalCost,
  reduce(total=0.0, r IN relationships(path) | total + r.rawCost) AS totalRawCost,
  [n IN nodes(path) | n.id] AS route

UNWIND range(0, size(route)-2) AS i
MATCH (:Country {id: route[i]})<-[:TO]-(cr:CountryRoute{month: $month})-[:FROM]->(:Country {id: route[i+1]})
WITH destination, route, totalCost, collect(cr.price) AS rawCosts

WITH
  destination,
  route[0] AS to,
  route[-1] AS from,
  totalCost,
  reduce(total=0.0, x IN rawCosts | total + x) AS totalRawCost,
  route

MERGE (c:Cost {
  id: from + '_' + to + '_' + $month
})
SET
c.value       = totalCost,
c.destination = $destId,
c.month       = $month,
c.rawValue    = totalRawCost

WITH c,from, to, totalCost, totalRawCost, route
MATCH (fromCountry:Country {id: from})
MERGE (fromCountry)-[:HAS_COST]->(c)

RETURN
  from,
  to,
  totalCost,
  totalRawCost,
  route
    ORDER BY totalCost ASC;