package com.example.EarthquakeCheck.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.EarthquakeCheck.entity.PgaValue;

@Repository
public interface PgaValueRepository extends JpaRepository<PgaValue, Long> {
    
    PgaValue findByLatitudeAndLongitude(double latitude, double longitude);
    
    @Query(value = "SELECT * FROM pga_value ORDER BY SQRT(POWER(latitude - :latitude, 2) + POWER(longitude - :longitude, 2)) ASC LIMIT 1", nativeQuery = true)
    PgaValue findNearestCoordinate(@Param("latitude") double latitude, @Param("longitude") double longitude);
} 