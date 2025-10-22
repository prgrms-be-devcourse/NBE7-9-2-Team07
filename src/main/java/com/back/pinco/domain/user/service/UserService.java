package com.back.pinco.domain.user.service;

import com.back.pinco.domain.bookmark.dto.BookmarkDto;
import com.back.pinco.domain.bookmark.entity.Bookmark;
import com.back.pinco.domain.bookmark.repository.BookmarkRepository;
import com.back.pinco.domain.bookmark.service.BookmarkService;
import com.back.pinco.domain.likes.repository.LikesRepository;
import com.back.pinco.domain.pin.dto.PinDto;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.repository.UserRepository;
import com.back.pinco.global.exception.ErrorCode;
import com.back.pinco.global.exception.ServiceException;
import com.back.pinco.global.rq.Rq;
import com.back.pinco.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final BookmarkRepository bookmarkRepository;
    private final LikesRepository likesRepository;
    private final PinRepository pinRepository;
    private final Rq rq;

    @Transactional
    public String ensureApiKey(User user) {
        if (user.getApiKey() == null || user.getApiKey().isBlank()) {
            String key = UUID.randomUUID().toString();

            // 혹시 중복이면 새로 생성
            while (userRepository.existsByApiKey(key)) {
                key = UUID.randomUUID().toString();
            }

            user.setApiKey(key);
            userRepository.save(user);
        }
        return user.getApiKey();
    }



    @Transactional
    public User createUser(String email, String password, String userName) {
        if (email == null || email.isBlank() ||
                !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new ServiceException(ErrorCode.INVALID_EMAIL_FORMAT);
        }
        if (password == null || password.isBlank() || password.length() < 8) {
            throw new ServiceException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }
        if (userName == null || userName.isBlank() || userName.length() < 2 || userName.length() > 20) {
            throw new ServiceException(ErrorCode.INVALID_USERNAME_FORMAT);
        }
        if (userRepository.existsByEmail(email)) {
            throw new ServiceException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUserName(userName)) {
            throw new ServiceException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        String hashedPwd = passwordEncoder.encode(password);
        User user = new User(email, hashedPwd, userName);
        userRepository.save(user);
        ensureApiKey(user);
        return user;
    }

    @Transactional(readOnly = true)
    public void login(String email, String rawPwd) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(rawPwd, user.getPassword())) {
            throw new ServiceException(ErrorCode.PASSWORD_NOT_MATCH);
        }
    }

    @Transactional
    public void editName(User user, String newUserName) {
        if (newUserName == null || newUserName.isBlank()
                || newUserName.length() < 2 || newUserName.length() > 20) {
            throw new ServiceException(ErrorCode.INVALID_USERNAME_FORMAT);
        }
        if (newUserName.equals(user.getUserName())) {
            return; // 변경 없음
        }
        // 자신 제외 중복 체크
        if (userRepository.existsByUserNameAndIdNot(newUserName, user.getId())) {
            throw new ServiceException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        user.setUserName(newUserName);
    }

    // 회원 정보 패스워드 수정
    @Transactional
    public void editPwd(User user, String newPassword) {
        if (newPassword == null || newPassword.isBlank() || newPassword.length() < 8) {
            throw new ServiceException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }
        if (newPassword.equals(user.getPassword())) {
            return; // 변경 없음
        }
        String hashedPwd = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPwd);
    }

    // 회원 정보 모두 수정
    @Transactional
    public void editAll(User user, String newUserName, String newPassword) {
        if (newUserName == null || newUserName.isBlank()
                || newUserName.length() < 2 || newUserName.length() > 20) {
            throw new ServiceException(ErrorCode.INVALID_USERNAME_FORMAT);
        }
        if (userRepository.existsByUserNameAndIdNot(newUserName, user.getId())) {
            throw new ServiceException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        if (newPassword == null || newPassword.isBlank() || newPassword.length() < 8) {
            throw new ServiceException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }
        if (newPassword.equals(user.getPassword()) && newUserName.equals(user.getUserName())) {
            return; // 변경 없음
        }
        user.setUserName(newUserName);
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    // 회원 정보 삭제
    @Transactional
    public void delete(User user) {
        userRepository.delete(user);
        userRepository.flush();
    }

    // 비밀번호 확인
    @Transactional
    public void checkPwd(User user, String pwd) {
        if(!passwordEncoder.matches(pwd, user.getPassword())) {
            throw new ServiceException(ErrorCode.PASSWORD_NOT_MATCH);
        }
    }

    // 이메일로 사용자 찾기
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void existsUserId(Long id) {
        if(!userRepository.existsById(id)) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Transactional
    public boolean nameChanged(User currentUser, String newUserName) {
        if(newUserName != null && !newUserName.isBlank() && !newUserName.trim().equals(currentUser.getUserName())) {
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public boolean passwordChanged(User currentUser, String newPassword) {
        if(newPassword != null && !newPassword.isBlank() && !passwordEncoder.matches(newPassword, currentUser.getPassword())) {
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public void editUserInfo(Long userId, String newUserName, String newPassword) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
        boolean nameChanged = nameChanged(currentUser, newUserName);
        boolean pwdChanged = passwordChanged(currentUser, newPassword);
        if (nameChanged && pwdChanged) {
            editAll(currentUser, newUserName, newPassword);
        } else if (nameChanged) {
            editName(currentUser, newUserName);
        } else if (pwdChanged) {
            editPwd(currentUser, newPassword);
        } else  {
            throw new ServiceException(ErrorCode.NO_FIELDS_TO_UPDATE);
        }
    }

    @Transactional(readOnly = true)
    public User findByApiKey(String apiKey) {
        return userRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_INFO_NOT_FOUND));
    }

    // accessToken 생성
    public String genAccessToken(User user) {
        return tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getUserName());
    }

    @Transactional(readOnly = true)
    public Optional<User> findByIdOptional(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Pin> getMyPins() {
        Long userId = rq.getActor().getId();
        return pinRepository.findAccessibleByUser(userId, userId);
    }

    @Transactional(readOnly = true)
    public List<Bookmark> getMyBookmarks() {
        User actor = rq.getActor();
        return bookmarkRepository.findByUserAndDeletedFalse(actor);
    }

    @Transactional
    public Long likesCount(List<Pin> listPin) {
        long totalLikesReceived = listPin.stream()
                .mapToLong(pin -> likesRepository.countByPin_IdAndLikedTrue(pin.getId()))
                .sum();
        return totalLikesReceived;
    }

    @Transactional(readOnly = true)
    public List<PinDto> publicList() {
        User actor = rq.getActor();
        List<Pin> publicPins = pinRepository.findPublicByUser(actor.getId());
        List<PinDto> publicList = publicPins.stream()
                .map(PinDto::new)
                .toList();
        return publicList;
    }

    @Transactional(readOnly = true)
    public List<PinDto> privateList() {
        List<PinDto> privatePins = getMyPins().stream()
                .filter(pin -> !Boolean.TRUE.equals(pin.getIsPublic())) // 비공개만 필터링
                .map(PinDto::new)
                .toList();
        return privatePins;
    }


}
