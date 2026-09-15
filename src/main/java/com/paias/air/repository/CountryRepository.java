package com.paias.air.repository;

import com.paias.air.model.n4j.heuristics.Country;
import com.paias.air.model.n4j.projection.CountryAirportTimezoneProjection;
import com.paias.air.model.n4j.projection.CountryProjection;
import com.paias.air.model.n4j.projection.CountryWithAirportsProjection;
import com.paias.air.model.n4j.projection.IdProjection;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepository extends Neo4jRepository<Country, String> {
    List<IdProjection> findAllIdProjectionsBy();
    List<CountryProjection> findAllCountryProjectionsBy();
    List<CountryAirportTimezoneProjection> findByIdIn(@Param("countryCodes") List<String> countryCodes);

    List<CountryWithAirportsProjection> findAllCountriesWithAirportsBy();
    @Query("""
            MATCH (a:Airport)-[:LOCATED_IN]->(c:Country)
            WHERE a.id = $airportCode
            RETURN c;
            """)
    IdProjection findCountryByAirport(@Param("airportCode") String airportCode);
}
