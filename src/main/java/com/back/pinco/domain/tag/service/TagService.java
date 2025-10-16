package com.back.pinco.domain.tag.service;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import com.back.pinco.domain.tag.entity.PinTag;
import com.back.pinco.domain.tag.entity.Tag;
import com.back.pinco.domain.tag.repository.PinTagRepository;
import com.back.pinco.domain.tag.repository.TagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final PinTagRepository pinTagRepository;
    private final PinRepository pinRepository; // 이미 존재한다고 가정

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

    // 핀에 태그 연결
    @Transactional
    public PinTag addTagToPin(Long pinId, String keyword) {

        // 핀 존재 여부 확인
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new EntityNotFoundException("핀을 찾을 수 없습니다."));

        // 태그 존재 여부 확인 (없으면 생성)
        Tag tag = tagRepository.findByKeyword(keyword)
                .orElseGet(() -> tagRepository.save(new Tag(keyword)));

        // 이미 이 핀에 연결된 태그인지 확인
        Optional<PinTag> existing = pinTagRepository.findByPin_IdAndTag_Id(pinId, tag.getId());

        if (existing.isPresent()) {
            PinTag pinTag = existing.get();

            // soft delete된 상태라면 복구
            if (pinTag.getIsDeleted()) {
                pinTag.restore();
                return pinTagRepository.save(pinTag);
            }

            // 이미 활성 상태로 존재하면 중복 추가 방지
            throw new IllegalStateException("이미 이 핀에 연결된 태그입니다.");
        }

        // 새로운 핀-태그 연결 생성
        PinTag newPinTag = new PinTag(pin, tag, false);
        return pinTagRepository.save(newPinTag);
    }

    // 핀에 연결된 태그 조회
    @Transactional
    public List<Tag> getTagsByPin(Long pinId) {
        return pinTagRepository.findAllByPin_IdAndIsDeletedFalse(pinId)
                .stream()
                .map(PinTag::getTag)
                .toList();
    }

    // 핀에서 태그 삭제 (Soft Delete)
    @Transactional
    public void removeTagFromPin(Long pinId, Long tagId) {
        PinTag pinTag = pinTagRepository.findByPin_IdAndTag_Id(pinId, tagId)
                .orElseThrow(() -> new RuntimeException("태그 연결이 없습니다."));
        pinTag.setIsDeleted();
    }

    // 삭제된 핀 복구
    @Transactional
    public void restoreTagFromPin(Long pinId, Long tagId) {
        PinTag pinTag = pinTagRepository.findByPin_IdAndTag_Id(pinId, tagId)
                .orElseThrow(() -> new RuntimeException("복구할 태그 연결이 없습니다."));

        if (!pinTag.getIsDeleted()) {
            throw new RuntimeException("이미 활성화된 태그입니다.");
        }

        pinTag.restore();
    }

    // 태그 키워드로 핀 조회
    @Transactional(readOnly = true)
    public List<Pin> getPinsByTagKeyword(String keyword) {
        // 우선 태그가 존재하는지 확인 (없으면 404)
        if (tagRepository.findByKeyword(keyword).isEmpty()) {
            throw new EntityNotFoundException("존재하지 않는 태그입니다.");
        }

        // 태그에 연결된 핀 목록 조회 (fetch join으로 lazy 문제 방지)
        return pinTagRepository.findPinsByTagKeyword(keyword);
    }
}

