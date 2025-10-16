package com.back.pinco.domain.tag.controller;

import com.back.pinco.domain.pin.dto.PinDto;
import com.back.pinco.domain.tag.dto.PinTagDto;
import com.back.pinco.domain.tag.dto.TagDto;
import com.back.pinco.domain.tag.entity.PinTag;
import com.back.pinco.domain.tag.service.PinTagService;
import com.back.pinco.domain.tag.service.TagService;
import com.back.pinco.global.rsData.RsData;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TagController {

    private final TagService tagService;
    private final PinTagService PinTagService;

    // 태그 전체 조회
    @GetMapping("/tags")
    public RsData<List<TagDto>> getAllTags() {
        List<TagDto> tags = tagService.getAllTags().stream()
                .map(TagDto::new)
                .toList();
        return new RsData<>("200", "태그 목록 조회 성공", tags);
    }

    // 특정 핀에 태그 추가
    @PostMapping("/pins/{pinId}/tags")
    public RsData<PinTagDto> addTagToPin(@PathVariable Long pinId,
                                         @RequestBody Map<String, String> request) {
        String keyword = request.get("keyword");
        try {
            PinTag pinTag = PinTagService.addTagToPin(pinId, keyword);
            return new RsData<>("200", "태그가 핀에 추가되었습니다.", new PinTagDto(pinTag));
        } catch (EntityNotFoundException e) {
            return new RsData<>("404", "핀을 찾을 수 없습니다.", null);
        } catch (IllegalStateException e) {
            return new RsData<>("409", "이미 이 핀에 연결된 태그입니다.", null);
        } catch (Exception e) {
            return new RsData<>("500", "알 수 없는 서버 오류가 발생했습니다.", null);
        }
    }

    // 핀에 연결된 태그 조회
    @GetMapping("/pins/{pinId}/tags")
    public RsData<List<TagDto>> getTagsByPin(@PathVariable Long pinId) {
        try {
            List<TagDto> tags = PinTagService.getTagsByPin(pinId).stream()
                    .map(TagDto::new)
                    .toList();

            if (tags.isEmpty()) {
                return new RsData<>("404", "해당 핀에 연결된 태그가 없습니다.", null);
            }
            return new RsData<>("200", "핀의 태그 목록 조회 성공", tags);
        } catch (EntityNotFoundException e) {
            return new RsData<>("404", "핀을 찾을 수 없습니다.", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new RsData<>("500", "태그 조회 중 오류가 발생했습니다.", null);
        }
    }

    // 태그 삭제 (Soft Delete)
    @DeleteMapping("/pins/{pinId}/tags/{tagId}")
    public RsData<Void> removeTagFromPin(@PathVariable Long pinId,
                                         @PathVariable Long tagId) {
        try {
            PinTagService.removeTagFromPin(pinId, tagId);
            return new RsData<>("200", "태그가 삭제되었습니다.", null);
        } catch (EntityNotFoundException e) {
            return new RsData<>("404", "삭제할 태그 또는 핀을 찾을 수 없습니다.", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new RsData<>("500", "태그 삭제 중 오류가 발생했습니다.", null);
        }
    }

    // 태그 복구(추후 관리자가 사용)
    @PatchMapping("/pins/{pinId}/tags/{tagId}/restore")
    public RsData<Void> restoreTagFromPin(@PathVariable Long pinId,
                                          @PathVariable Long tagId) {
        try {
            PinTagService.restoreTagFromPin(pinId, tagId);
            return new RsData<>("200", "태그가 복구되었습니다.", null);
        } catch (EntityNotFoundException e) {
            return new RsData<>("404", "복구할 태그 또는 핀을 찾을 수 없습니다.", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new RsData<>("500", "태그 복구 중 오류가 발생했습니다.", null);
        }
    }

    // 태그 키워드로 핀 조회
    @GetMapping("/tags/{keyword}/pins")
    public RsData<List<PinDto>> getPinsByTag(@PathVariable String keyword) {
        try {
            List<PinDto> pins = PinTagService.getPinsByTagKeyword(keyword)
                    .stream()
                    .map(PinDto::new)
                    .toList();

            if (pins.isEmpty()) {
                return new RsData<>("404", "해당 태그가 달린 게시물이 없습니다.", null);
            }

            return new RsData<>("200", "태그 기반 게시물 목록 조회 성공", pins);

        } catch (EntityNotFoundException e) {
            return new RsData<>("404", "존재하지 않는 태그입니다.", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new RsData<>("500", "태그 기반 게시물 조회 중 오류가 발생했습니다.", null);
        }
    }
}
