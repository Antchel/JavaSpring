package com.agolovenko.jspring.OSM.REST.persist;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NodeRepository extends CrudRepository<Node, Long> {
    @Modifying
    @Query("FROM Node " +
            "where (earth_distance(ll_to_earth(lat,lon), ll_to_earth((:lat) ,(:lon)))) < (:distanceInMeters)" +
            "ORDER BY (earth_distance(ll_to_earth(lat,lon), ll_to_earth((:lat) ,(:lon)))) ASC")
    List<Node> getNodesByDistance(@Param("lat") Float lat,
                                  @Param("lon") Float lon,
                                  @Param("distanceInMeters") Integer distanceInMeters);

    @Modifying
    @Query(nativeQuery = true, value = "SELECT * FROM Node " +
            "where ((earth_distance(ll_to_earth(lat,lon), ll_to_earth((:lat) ,(:lon)))) < (:distanceInMeters) and (json_array_length(tags) > 2) and (to_jsonb(tags) @> '[{\"k\":\"amenity\",\"v\":\":amenity\"}]'))" +
            " ORDER BY (earth_distance(ll_to_earth(lat,lon), ll_to_earth((:lat) ,(:lon)))) ASC")
    List<Node> getNodesByAmenity(@Param("lat") Float lat,
                                 @Param("lon") Float lon,
                                 @Param("amenity") String amenity,
                                 @Param("distanceInMeters") Integer distanceInMeters);
}
