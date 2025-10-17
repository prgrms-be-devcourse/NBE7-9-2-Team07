package com.back.pinco.domain.tag.service;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import com.back.pinco.domain.tag.entity.PinTag;
import com.back.pinco.domain.tag.entity.Tag;
import com.back.pinco.domain.tag.repository.PinTagRepository;
import com.back.pinco.domain.tag.repository.TagRepository;
import com.back.pinco.global.exception.ErrorCode;
import com.back.pinco.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PinTagService {

    private final TagService tagService;
    private final TagRepository tagRepository;
    private final PinTagRepository pinTagRepository;
    private final PinRepository pinRepository;

    // 핀에 태그 연결
    @Transactional
    public PinTag addTagToPin(Long pinId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ServiceException(ErrorCode.INVALID_TAG_KEYWORD);
        }

        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ServiceException(ErrorCode.TAG_PIN_NOT_FOUND));

        Tag tag = tagRepository.findByKeyword(keyword)
                .orElseGet(() -> tagRepository.save(new Tag(keyword)));

        var existing = pinTagRepository.findByPin_IdAndTag_Id(pinId, tag.getId());
        if (existing.isPresent()) {
            PinTag pinTag = existing.get();
            if (pinTag.getIsDeleted()) {
                pinTag.restore();
                return pinTagRepository.save(pinTag);
            }
            throw new ServiceException(ErrorCode.TAG_ALREADY_LINKED);
        }

        try {
            return pinTagRepository.save(new PinTag(pin, tag, false));
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.TAG_CREATE_FAILED);
        }
    }

    // 핀에 연결된 태그 조회
    @Transactional(readOnly = true)
    public List<Tag> getTagsByPin(Long pinId) {
        if (!pinRepository.existsById(pinId)) {
            throw new ServiceException(ErrorCode.TAG_PIN_NOT_FOUND);
        }

        List<Tag> tags = pinTagRepository.findAllByPin_IdAndIsDeletedFalse(pinId)
                .stream()
                .map(PinTag::getTag)
                .toList();

        if (tags.isEmpty()) {
            throw new ServiceException(ErrorCode.PIN_TAG_LIST_EMPTY);
        }

        return tags;
    }

    // 태그 삭제
    @Transactional
    public void removeTagFromPin(Long pinId, Long tagId) {
        PinTag pinTag = pinTagRepository.findByPin_IdAndTag_Id(pinId, tagId)
                .orElseThrow(() -> new ServiceException(ErrorCode.TAG_LINK_NOT_FOUND));
        try {
            pinTag.setIsDeleted();
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.PIN_TAG_DELETE_FAILED);
        }
    }

    // 태그 복구
    @Transactional
    public void restoreTagFromPin(Long pinId, Long tagId) {
        PinTag pinTag = pinTagRepository.findByPin_IdAndTag_Id(pinId, tagId)
                .orElseThrow(() -> new ServiceException(ErrorCode.TAG_LINK_NOT_FOUND));

        if (!pinTag.getIsDeleted()) {
            throw new ServiceException(ErrorCode.TAG_ALREADY_LINKED);
        }

        try {
            pinTag.restore();
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.PIN_TAG_RESTORE_FAILED);
        }
    }

    // 태그 키워드로 핀 조회
    @Transactional(readOnly = true)
    public List<Pin> getPinsByTagKeyword(String keyword) {
        Tag tag = tagRepository.findByKeyword(keyword)
                .orElseThrow(() -> new ServiceException(ErrorCode.TAG_NOT_FOUND));

        List<Pin> pins = pinTagRepository.findPinsByTagKeyword(keyword);
        if (pins.isEmpty()) {
            throw new ServiceException(ErrorCode.TAG_POSTS_NOT_FOUND);
        }

        return pins;
    }

    // 새로운 게시글 등록시 사용할 메서드
    @Transactional
    public List<Tag> linkTagsToPin(Long pinId, List<String> tagKeywords) {
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ServiceException(ErrorCode.TAG_PIN_NOT_FOUND));

        List<Tag> linkedTags = new ArrayList<>();

        for (String keyword : tagKeywords) {
            Tag tag = tagRepository.findByKeyword(keyword)
                    .orElseGet(() -> tagRepository.save(new Tag(keyword)));

            pinTagRepository.findByPin_IdAndTag_Id(pinId, tag.getId())
                    .ifPresentOrElse(
                            existing -> { if (existing.getIsDeleted()) existing.restore(); },
                            () -> pinTagRepository.save(new PinTag(pin, tag, false))
                    );

            linkedTags.add(tag);
        }

        return linkedTags; // PinController에서 DTO 구성에 사용
    }
}
