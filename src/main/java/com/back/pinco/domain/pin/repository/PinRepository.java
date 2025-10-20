package com.back.pinco.domain.pin.repository;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.user.entity.User;
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
    ) AND p.is_public=:isPublicCheck AND p.is_deleted=:isDeletedCheck
    """,
            nativeQuery = true)
    List<Pin> findPinsWithinRadius(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusInMeters") Double radiusInMeters,
            @Param("isPublicCheck") Boolean isPublicCheck,
            @Param("isDeletedCheck") Boolean isDeletedCheck
    );



    // 특정 사용자의 핀 조회
    List<Pin> findByUserAndIsPublicAndDeleted(User user, Boolean isPublic, Boolean deleted);

    // 전체 핀 조회
    List<Pin> findByIsPublicAndDeleted(Boolean isPublic, Boolean deleted);

    @Query("SELECT p FROM Pin p " +
            "WHERE p.id = :id AND p.isPublic = :isPublic AND p.deleted = :isDeleted")
    Pin findByIdWithConditions(
                                        @Param("id") Long id,
                                        @Param("isPublic") Boolean isPublic,
                                        @Param("isDeleted") Boolean isDeleted
    );
}
