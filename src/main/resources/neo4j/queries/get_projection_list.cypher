CALL gds.graph.list()
YIELD graphName, nodeCount, relationshipCount,creationTime,sizeInBytes
RETURN graphName, nodeCount, relationshipCount,creationTime,sizeInBytes;