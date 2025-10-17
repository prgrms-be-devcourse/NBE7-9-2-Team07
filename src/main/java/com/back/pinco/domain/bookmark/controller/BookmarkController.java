package com.back.pinco.domain.bookmark.controller;

import com.back.pinco.domain.bookmark.dto.BookmarkDto;
import com.back.pinco.domain.bookmark.dto.BookmarkRequestDto;
import com.back.pinco.domain.bookmark.service.BookmarkService;
import com.back.pinco.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    // 북마크 생성 API
    @PostMapping
    public RsData<BookmarkDto> createBookmark(
            @RequestBody BookmarkRequestDto requestDto
    ) {
        BookmarkDto bookmarkDto = bookmarkService.createBookmark(requestDto.userId(), requestDto.pinId());
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다.",
                bookmarkDto
        );
    }
}
