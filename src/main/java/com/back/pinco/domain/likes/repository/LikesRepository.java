package com.back.pinco.domain.likes.repository;

import com.back.pinco.domain.likes.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<Likes, Long> {

    boolean existsByPinIdAndUserId(Long pinId, Long userId);
    Optional<Likes> findByPinIdAndUserId(Long pinId, Long userId);

    long countByPinId(Long pinId);
}
