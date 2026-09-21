CREATE INDEX searchrecord_from_idx IF NOT EXISTS
FOR (s:SearchRecord)
ON (s.from);

CREATE INDEX searchrecord_to_idx IF NOT EXISTS
FOR (s:SearchRecord)
ON (s.to);

CREATE INDEX searchrecord_dateFrom_idx IF NOT EXISTS
FOR (s:SearchRecord)
ON (s.dateFrom);

CREATE INDEX searchrecord_searchType_idx IF NOT EXISTS
FOR (s:SearchRecord)
ON (s.searchType);

CREATE INDEX flight_fromAirport IF NOT EXISTS
FOR (f:Flight)
ON (f.fromAirport);

CREATE INDEX flight_toAirport IF NOT EXISTS
FOR (f:Flight)
ON (f.toAirport);

CREATE INDEX flight_toTime IF NOT EXISTS
FOR (f:Flight)
ON (f.toTime);

CREATE INDEX flight_from_to_country_time IF NOT EXISTS
FOR (f:Flight)
ON (f.fromCountry, f.toCountry, f.toTime);