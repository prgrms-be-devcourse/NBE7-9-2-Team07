package com.back.pinco.domain.tag.service;

import com.back.pinco.domain.tag.entity.Tag;
import com.back.pinco.domain.tag.repository.TagRepository;
import com.back.pinco.global.exception.ErrorCode;
import com.back.pinco.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public Tag getOrCreateTag(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ServiceException(ErrorCode.INVALID_TAG_KEYWORD);
        }
        return tagRepository.findByKeyword(keyword)
                .orElseGet(() -> tagRepository.save(new Tag(keyword)));
    }

    public List<Tag> getAllTags() {
        List<Tag> tags = tagRepository.findAll();

        if (tags.isEmpty()) {
            throw new ServiceException(ErrorCode.TAG_NOT_FOUND);
        }

        return tags;
    }

    @Transactional(readOnly = true)
    public boolean existsByKeyword(String keyword) {
        return tagRepository.existsByKeyword(keyword);
    }

    @Transactional
    public Tag createTag(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ServiceException(ErrorCode.INVALID_TAG_KEYWORD);
        }
        if (tagRepository.existsByKeyword(keyword)) {
            throw new ServiceException(ErrorCode.TAG_ALREADY_EXISTS);
        }
        try {
            return tagRepository.save(new Tag(keyword.trim()));
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.TAG_CREATE_FAILED);
        }
    }
}
