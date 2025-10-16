package com.back.pinco.domain.tag.repository;

import com.back.pinco.domain.tag.entity.PinTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PinTagRepository extends JpaRepository<PinTag, Long> {
    List<PinTag> findAllByPin_IdAndIsDeletedFalse(Long pinId);

    Optional<PinTag> findByPin_IdAndTag_Id(Long pinId, Long tagId);
}

