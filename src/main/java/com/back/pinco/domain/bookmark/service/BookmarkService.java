package com.back.pinco.domain.bookmark.service;

import com.back.pinco.domain.bookmark.dto.BookmarkDto;
import com.back.pinco.domain.bookmark.entity.Bookmark;
import com.back.pinco.domain.bookmark.repository.BookmarkRepository;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.service.UserService;
import com.back.pinco.global.exception.ErrorCode;
import com.back.pinco.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final UserService userService;
    private final PinService pinService;

    // 북마크 생성
    public BookmarkDto createBookmark(Long userId, Long pinId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
        Pin pin = pinService.findById(pinId);

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserAndPinAndIsDeletedFalse(user, pin);
        if (existingBookmark.isPresent()) {
            throw new ServiceException(ErrorCode.BOOKMARK_ALREADY_EXISTS);
        }

        Bookmark bookmark = new Bookmark(user, pin);
        try {
            bookmarkRepository.save(bookmark);
        } catch (Exception ex) {
            throw new ServiceException(ErrorCode.BOOKMARK_CREATE_FAILED);
        }
        return new BookmarkDto(bookmark);
    }

    // 나의 북마크 목록 조회
    public List<BookmarkDto> getMyBookmarks(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        // 삭제되지 않은 북마크 목록만 조회
        List<Bookmark> bookmarks = bookmarkRepository.findByUserAndIsDeletedFalse(user);

        return bookmarks.stream()
                .map(BookmarkDto::new)
                .collect(Collectors.toList());
    }

    // 북마크 삭제
    public void deleteBookmark(Long userId, Long bookmarkId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new ServiceException(ErrorCode.BOOKMARK_NOT_FOUND));

        // 소유자 확인(jwt 적용 시 수정)
        if (!bookmark.getUser().getId().equals(user.getId())) {
            // 소유자가 아니면 찾을 수 없음으로 처리
            throw new ServiceException(ErrorCode.BOOKMARK_NOT_FOUND);
        }

        try {
            bookmark.setIsDeleted();
            bookmarkRepository.save(bookmark);
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.BOOKMARK_DELETE_FAILED);
        }
    }

    // 북마크 복원
    public void restoreBookmark(Long userId, Long bookmarkId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new ServiceException(ErrorCode.BOOKMARK_NOT_FOUND));

        // 소유자 확인(jwt 적용 시 수정)
        if (!bookmark.getUser().getId().equals(user.getId())) {
            throw new ServiceException(ErrorCode.BOOKMARK_NOT_FOUND);
        }

        try {
            bookmark.restore();
            bookmarkRepository.save(bookmark);
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.BOOKMARK_RESTORE_FAILED);
        }
    }


}
