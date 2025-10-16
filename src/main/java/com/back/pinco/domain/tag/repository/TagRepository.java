package com.back.pinco.domain.tag.repository;

import com.back.pinco.domain.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByKeyword(String keyword);

    List<Tag> findByKeywordContainingIgnoreCase(String keyword);

    List<Tag> findByKeywordIn(List<String> keywords);
}

