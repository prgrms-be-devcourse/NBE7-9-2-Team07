package com.back.pinco.domain.tag.controller;

import com.back.pinco.domain.tag.dto.PinTagDto;
import com.back.pinco.domain.tag.dto.TagDto;
import com.back.pinco.domain.tag.entity.PinTag;
import com.back.pinco.domain.tag.service.TagService;
import com.back.pinco.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TagController {

    private final TagService tagService;

    // ✅ 태그 전체 조회
    @GetMapping("/tags")
    public RsData<List<TagDto>> getAllTags() {
        List<TagDto> tags = tagService.getAllTags().stream()
                .map(TagDto::new)
                .toList();
        return new RsData<>("200", "태그 목록 조회 성공", tags);
    }

    // ✅ 특정 핀에 태그 추가
    @PostMapping("/pins/{pinId}/tags")
    public RsData<PinTagDto> addTagToPin(@PathVariable Long pinId,
                                         @RequestBody Map<String, String> request) {
        String keyword = request.get("keyword");
        PinTag pinTag = tagService.addTagToPin(pinId, keyword);
        return new RsData<>("200", "태그가 핀에 추가되었습니다.", new PinTagDto(pinTag));
    }

    // ✅ 핀에 연결된 태그 조회
    @GetMapping("/pins/{pinId}/tags")
    public RsData<List<TagDto>> getTagsByPin(@PathVariable Long pinId) {
        List<TagDto> tags = tagService.getTagsByPin(pinId).stream()
                .map(TagDto::new)
                .toList();

        if (tags.isEmpty()) {
            return new RsData<>("404", "해당 핀에 연결된 태그가 없습니다.", null);
        }

        return new RsData<>("200", "핀의 태그 목록 조회 성공", tags);
    }

    // ✅ 태그 삭제 (Soft Delete)
    @DeleteMapping("/pins/{pinId}/tags/{tagId}")
    public RsData<Void> removeTagFromPin(@PathVariable Long pinId,
                                         @PathVariable Long tagId) {
        tagService.removeTagFromPin(pinId, tagId);
        return new RsData<>("200", "태그가 삭제되었습니다.", null);
    }

    // ✅ 태그 복구
    @PatchMapping("/pins/{pinId}/tags/{tagId}/restore")
    public RsData<Void> restoreTagFromPin(@PathVariable Long pinId,
                                          @PathVariable Long tagId) {
        tagService.restoreTagFromPin(pinId, tagId);
        return new RsData<>("200", "태그가 복구되었습니다.", null);
    }
}
