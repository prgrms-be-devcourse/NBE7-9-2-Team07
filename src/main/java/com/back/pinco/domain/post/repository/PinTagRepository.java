package com.back.pinco.domain.post.repository;

import com.back.pinco.domain.post.entity.PinTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PinTagRepository extends JpaRepository<PinTag, Long> {
}
