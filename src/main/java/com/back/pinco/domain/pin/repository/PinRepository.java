package com.back.pinco.domain.pin.repository;

import com.back.pinco.domain.pin.entity.Pin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PinRepository extends JpaRepository<Pin,Long> {

    @Query(value = """
    SELECT * FROM pins p
    WHERE p.is_public = true AND ST_DWithin(
        p.point,
        ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
        :radiusInMeters
    )
    """,
            nativeQuery = true)
    List<Pin> findPinsWithinRadius(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusInMeters") Double radiusInMeters
    );
}
