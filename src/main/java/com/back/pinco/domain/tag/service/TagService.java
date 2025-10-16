package com.back.pinco.domain.tag.service;

import com.back.pinco.domain.tag.entity.Tag;
import com.back.pinco.domain.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    // 태그 생성 또는 조회
    @Transactional
    public Tag getOrCreateTag(String keyword) {
        return tagRepository.findByKeyword(keyword)
                .orElseGet(() -> tagRepository.save(new Tag(keyword)));
    }

    // 모든 태그 조회
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    // 특정 키워드로 검색
    public List<Tag> searchTags(String keyword) {
        return tagRepository.findByKeywordContainingIgnoreCase(keyword);
    }

    @Transactional
    public boolean existsByKeyword(String keyword) {
        return tagRepository.existsByKeyword(keyword);
    }

    @Transactional
    public Tag createTag(String keyword) {
        Tag tag = new Tag(keyword.trim());
        return tagRepository.save(tag);
    }
}

