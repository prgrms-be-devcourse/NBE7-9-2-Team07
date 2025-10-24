package com.back.pinco.domain.pin.repository;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.global.geometry.GeometryUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PinRepository extends JpaRepository<Pin,Long> {

    String BASE_QUERY =
            "SELECT * FROM pins p " +
                    "WHERE ST_DWithin(p.point, ST_SetSRID(ST_MakePoint(:longitude, :latitude), "
                    + GeometryUtil.SRID +
                    ")::geography, :radiusInMeters) " +
                    "AND p.is_deleted = false ";

    @Query(value = BASE_QUERY + "AND (user_id = :userId OR is_public = true)", nativeQuery = true)
    List<Pin> findPinsWithinRadius(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusInMeters") Double radiusInMeters,
            @Param("userId") Long userId
    );

    @Query(value = BASE_QUERY + "AND is_public = true", nativeQuery = true)
    List<Pin> findPublicPinsWithinRadius(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusInMeters") Double radiusInMeters
    );




    // 특정 사용자의 핀 조회
    @Query(value = """
    SELECT p FROM Pin p
    WHERE p.user.id = :writerId
      AND (p.user.id = :actorId OR p.isPublic = true)
      AND p.deleted=false 
""")
    List<Pin> findAccessibleByUser(Long writerId, Long actorId);

    @Query(value = """
    SELECT p FROM Pin p
    WHERE p.user.id = :writerId
      AND p.isPublic = true
      AND p.deleted=false 
""")
    List<Pin> findPublicByUser(Long writerId);

    // 전체 핀 조회
    @Query(value = """
    SELECT p FROM Pin p
    WHERE p.deleted = false
      AND (p.user.id = :userId OR p.isPublic = true)
""")
    List<Pin> findAllAccessiblePins(@Param("userId") Long userId);

    @Query(value = """
    SELECT p FROM Pin p
    WHERE p.deleted = false
      AND p.isPublic = true
""")
    List<Pin> findAllPublicPins();

    // id로 핀 조회
    @Query("""
    SELECT p FROM Pin p
    WHERE p.id = :id
      AND p.deleted = false
      AND (p.user.id = :userId OR p.isPublic = true)
""")
    Optional<Pin> findAccessiblePinById(@Param("id") Long id, @Param("userId") Long userId);
    @Query("""
    SELECT p FROM Pin p
    WHERE p.id = :id
      AND p.deleted = false
      AND p.isPublic = true
""")
    Optional<Pin> findPublicPinById(@Param("id") Long id);



}
