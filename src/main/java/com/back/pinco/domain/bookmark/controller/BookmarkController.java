package com.back.pinco.domain.bookmark.controller;

import com.back.pinco.domain.bookmark.dto.BookmarkDto;
import com.back.pinco.domain.bookmark.dto.createBookmarkRequest;
import com.back.pinco.domain.bookmark.service.BookmarkService;
import com.back.pinco.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    // 북마크 생성 API
    @PostMapping
    public RsData<BookmarkDto> createBookmark(
            @RequestBody createBookmarkRequest requestDto
    ) {
        BookmarkDto bookmarkDto = bookmarkService.addBookmark(requestDto.userId(), requestDto.pinId());
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다.",
                bookmarkDto
        );
    }

    // 나의 북마크 목록 조회
    @GetMapping
    public RsData<List<BookmarkDto>> getMyBookmarks(@RequestParam Long userId) {
        List<BookmarkDto> bookmarkDtos = bookmarkService.getMyBookmarks(userId);

        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다.",
                bookmarkDtos
        );
    }

    // 북마크 삭제 (soft delete)
    @DeleteMapping("/{bookmarkId}")
    public RsData<Void> deleteBookmark(@PathVariable Long bookmarkId, @RequestParam Long userId) {
        bookmarkService.deleteBookmark(userId, bookmarkId);
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다."
        );
    }

    // 북마크 복원
    @PatchMapping("/{bookmarkId}")
    public RsData<Void> restoreBookmark(@PathVariable Long bookmarkId, @RequestParam Long userId) {
        bookmarkService.restoreBookmark(userId, bookmarkId);
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다."
        );
    }

}
