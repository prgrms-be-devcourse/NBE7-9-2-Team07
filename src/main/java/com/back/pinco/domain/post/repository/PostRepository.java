package com.back.pinco.domain.post.repository;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Long> {
    Optional<List<Post>> findByPin(Pin pin);
}
