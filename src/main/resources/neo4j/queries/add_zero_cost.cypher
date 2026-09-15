MATCH (destination:Country {id: $destId})

MERGE (c:Cost {
  id: $destId + '_' + $month
})
    ON CREATE SET
    c.value        = 0.0,
    c.rawValue     = 0.0,
    c.destination  = $destId,
    c.month        = $month
    ON MATCH SET
    c.value        = 0.0,
    c.rawValue     = 0.0

MERGE (destination)-[:HAS_COST]->(c);