package com.back.pinco.domain.user.service;

import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.repository.UserRepository;
import com.back.pinco.global.exception.ErrorCode;
import com.back.pinco.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional
    public User createUser(String email, String password, String userName) {
        // 1) 입력 검증(형식/길이)
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

        // 2) 중복 체크(이메일/닉네임)
        if (userRepository.existsByEmail(email)) {
            throw new ServiceException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUserName(userName)) {
            throw new ServiceException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        // 3) 저장
        String hashedPwd = passwordEncoder.encode(password);
        User user = new User(email, hashedPwd, userName);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public void login(String email, String rawPwd) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(rawPwd, user.getPassword())) {
            throw new ServiceException(ErrorCode.PASSWORD_NOT_MATCH);
        }
    }

    @Transactional(readOnly = true)
    public User userInform(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_INFO_NOT_FOUND));
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
    public void editPwd(User user, String pwd) {
        if (pwd == null || pwd.isBlank() || pwd.length() < 8) {
            throw new ServiceException(ErrorCode.INVALID_PASSWORD_FORMAT);
        } else {
            String hashedPwd = passwordEncoder.encode(pwd);
            user.setPassword(hashedPwd);
        }
    }

    // 회원 정보 모두 수정
    @Transactional
    public void editAll(User user, String username, String pwd) {
        user.setUserName(username);
        user.setPassword(passwordEncoder.encode(pwd));
    }

    // 회원 정보 삭제
    @Transactional
    public void delete(User user) {
        userRepository.delete(user);
    }

    @Transactional
    public boolean checkExist(String email) {
        if (!userRepository.existsByEmail(email)) {
            return false;
        } else {
            return true;
        }
    }

    // 비밀번호 확인
    @Transactional
    public boolean checkPwd(User user, String pwd) {
        return passwordEncoder.matches(pwd, user.getPassword());
    }

    // 이메일로 사용자 찾기
    @Transactional
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public boolean existsUserId(Long id) {
        return userRepository.existsById(id);
    }
}
