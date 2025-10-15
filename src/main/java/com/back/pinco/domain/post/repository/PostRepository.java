package com.back.pinco.domain.post.repository;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Long> {
    void deleteByPin(Pin pin);

    Optional<Post> findByPinId(Long pinId);
}
