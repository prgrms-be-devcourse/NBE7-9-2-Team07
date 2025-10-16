package com.back.pinco.domain.tag.repository;

import com.back.pinco.domain.tag.entity.PinTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PinTagRepository extends JpaRepository<PinTag, Long> {
}
