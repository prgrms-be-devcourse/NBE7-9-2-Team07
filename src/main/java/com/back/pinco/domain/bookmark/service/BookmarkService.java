package com.back.pinco.domain.bookmark.service;

import com.back.pinco.domain.bookmark.dto.BookmarkDto;
import com.back.pinco.domain.bookmark.entity.Bookmark;
import com.back.pinco.domain.bookmark.repository.BookmarkRepository;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final PinRepository pinRepository;

    public BookmarkDto createBookmark(Long userId, Long pinId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 유저 ID: " + userId));
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 핀 ID: " + pinId));

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserAndPinAndIsDeletedFalse(user, pin);
        if (existingBookmark.isPresent()) {
            throw new IllegalStateException("이미 북마크된 핀입니다.");
        }

        Bookmark bookmark = new Bookmark(user, pin);
        bookmarkRepository.save(bookmark);
        return new BookmarkDto(bookmark);
    }

    public List<BookmarkDto> getMyBookmarks(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 삭제되지 않은 북마크 목록만 조회
        List<Bookmark> bookmarks = bookmarkRepository.findByUserAndIsDeletedFalse(user);

        return bookmarks.stream()
                .map(BookmarkDto::new)
                .collect(Collectors.toList());
    }

}
