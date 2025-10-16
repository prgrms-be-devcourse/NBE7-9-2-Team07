package com.back.pinco.domain.tag.service;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import com.back.pinco.domain.tag.entity.PinTag;
import com.back.pinco.domain.tag.entity.Tag;
import com.back.pinco.domain.tag.repository.PinTagRepository;
import com.back.pinco.domain.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final PinTagRepository pinTagRepository;
    private final PinRepository pinRepository; // 이미 존재한다고 가정

    // ✅ 태그 생성 또는 조회
    @Transactional
    public Tag getOrCreateTag(String keyword) {
        return tagRepository.findByKeyword(keyword)
                .orElseGet(() -> tagRepository.save(new Tag(keyword)));
    }

    // ✅ 모든 태그 조회
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    // ✅ 특정 키워드로 검색
    public List<Tag> searchTags(String keyword) {
        return tagRepository.findByKeywordContainingIgnoreCase(keyword);
    }

    // ✅ 핀에 태그 연결
    @Transactional
    public PinTag addTagToPin(Long pinId, String keyword) {
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new RuntimeException("핀을 찾을 수 없습니다."));
        Tag tag = getOrCreateTag(keyword);

        return pinTagRepository.findByPin_IdAndTag_Id(pin.getId(), tag.getId())
                .orElseGet(() -> pinTagRepository.save(new PinTag(pin, tag)));
    }

    // ✅ 핀에 연결된 태그 조회
    @Transactional
    public List<Tag> getTagsByPin(Long pinId) {
        return pinTagRepository.findAllByPin_IdAndIsDeletedFalse(pinId)
                .stream()
                .map(PinTag::getTag)
                .toList();
    }

    // ✅ 핀에서 태그 삭제 (Soft Delete)
    @Transactional
    public void removeTagFromPin(Long pinId, Long tagId) {
        PinTag pinTag = pinTagRepository.findByPin_IdAndTag_Id(pinId, tagId)
                .orElseThrow(() -> new RuntimeException("태그 연결이 없습니다."));
        pinTag.setIsDeleted();
    }

    @Transactional
    public void restoreTagFromPin(Long pinId, Long tagId) {
        PinTag pinTag = pinTagRepository.findByPin_IdAndTag_Id(pinId, tagId)
                .orElseThrow(() -> new RuntimeException("복구할 태그 연결이 없습니다."));

        if (!pinTag.getIsDeleted()) {
            throw new RuntimeException("이미 활성화된 태그입니다.");
        }

        pinTag.restore(); // 👈 PinTag 엔티티에 이미 있는 복구 메서드
    }

    public List<Tag> searchTagsByMultipleKeywords(List<String> keywords) {
        return tagRepository.findByKeywordIn(keywords);
    }
}

