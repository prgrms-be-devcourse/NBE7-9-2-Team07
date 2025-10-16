package com.back.pinco.domain.tag.repository;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.tag.entity.PinTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PinTagRepository extends JpaRepository<PinTag, Long> {
    List<PinTag> findAllByPin_IdAndIsDeletedFalse(Long pinId);

    Optional<PinTag> findByPin_IdAndTag_Id(Long pinId, Long tagId);

    List<PinTag> findByTag_IdAndIsDeletedFalse(Long id);

    @Query("""
                SELECT p
                FROM PinTag pt
                JOIN pt.pin p
                JOIN pt.tag t
                WHERE t.keyword = :keyword
                  AND pt.isDeleted = false
            """)
    List<Pin> findPinsByTagKeyword(@Param("keyword") String keyword);
}

