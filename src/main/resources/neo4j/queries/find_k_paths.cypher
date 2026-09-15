CALL gds.graph.exists('path-search-graph-' + $destId + '-' + $month) YIELD exists AS destExists
CALL gds.graph.exists('path-search-graph-' + $sourceId + '-' + $month) YIELD exists AS sourceExists
WITH destExists, sourceExists
  WHERE destExists AND sourceExists

MATCH (source:Country {id: $sourceId})
MATCH (destination:Country {id: $destId})

CALL gds.shortestPath.yens.stream(
'path-search-graph-' + $destId + '-' + $month,
{
  sourceNode:                 source,
  targetNode:                 destination,
  k:                          50,
  relationshipWeightProperty: 'cost'
}
)
YIELD index, totalCost, nodeIds, path
RETURN
  index,
  totalCost,
  [n IN nodes(path) | n.id] AS route
  ORDER BY index ASC;