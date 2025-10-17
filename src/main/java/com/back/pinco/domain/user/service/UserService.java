package com.back.pinco.domain.user.service;

import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.repository.UserRepository;
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

    // 회원 가입
    @Transactional
    public User createUser(String email, String password, String userName) {
        String hashedPwd = passwordEncoder.encode(password);
        User user = new User(email,hashedPwd, userName);
        userRepository.save(user);
        return user;
    }

    // 로그인
    @Transactional(readOnly = true)
    public boolean login(String email, String pwd) {
        // 사용자 존재 여부 확인
        Optional<User> loginUser = userRepository.findByEmail(email);
        // 비밀번호 비교 (암호화된 값과 평문 비교)
        return passwordEncoder.matches(pwd, loginUser.get().getPassword());
    }

    // 회원 정보 조회
    @Transactional(readOnly = true)
    public Optional<User> userInform(Long id) {
        return userRepository.findById((id));
    }

    // 회원 정보 이름 수정
    @Transactional
    public void editName(User user, String username) {
        user.setUserName(username);
    }

    // 회원 정보 패스워드 수정
    @Transactional
    public void editPwd(User user, String pwd) {
        String hashedPwd = passwordEncoder.encode(pwd);
        user.setPassword(hashedPwd);
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

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
