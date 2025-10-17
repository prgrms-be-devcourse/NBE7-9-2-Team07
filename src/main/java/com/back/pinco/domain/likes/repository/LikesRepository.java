package com.back.pinco.domain.likes.repository;

import com.back.pinco.domain.likes.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<Likes, Long> {

    /**
     * 특정 핀에 대한 특정 사용자의 좋아요 존재 여부 확인
     * @param pinId
     * @param userId
     * @return boolean
     */
    boolean existsByPinIdAndUserId(Long pinId, Long userId);

    /**
     * 특정 핀에 대한 특정 사용자의 좋아요 조회
     * @param pinId
     * @param userId
     * @return Optional<Likes>
     */
    Optional<Likes> findByPinIdAndUserId(Long pinId, Long userId);

    /**
     * 특정 핀의 좋아요 개수 조회
     * @param pinId
     * @return long
     */
    long countByPinId(Long pinId);

    /**
     * 특정 핀에 대한 좋아요 존재 여부 확인
     * @param pinId
     * @return boolean
     */
    boolean existsByPinId(Long pinId);
}
