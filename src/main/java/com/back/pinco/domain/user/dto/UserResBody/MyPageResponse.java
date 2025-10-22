package com.back.pinco.domain.user.dto.UserResBody;

import com.back.pinco.domain.bookmark.dto.BookmarkDto;
import com.back.pinco.domain.pin.dto.PinDto;
import com.back.pinco.domain.user.dto.UserDto;

import java.util.List;

public record MyPageResponse(
        String email,
        String userName,
        int myPinCount,
        List<PinDto> myPins,
        int bookmarkCount,
        List<PinDto> bookmarkedPins,
        long totalLikesReceived
) {
    public MyPageResponse(UserDto userDto,
                          List<PinDto> myPins,
                          List<PinDto> bookmarkedPins,
                          long totalLikesReceived) {
        this(
                userDto.email(),
                userDto.userName(),
                myPins.size(),
                myPins,
                bookmarkedPins.size(),
                bookmarkedPins,
                totalLikesReceived
        );
    }
}

