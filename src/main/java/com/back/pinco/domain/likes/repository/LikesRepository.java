package com.back.pinco.domain.likes.repository;

import com.back.pinco.domain.likes.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<Likes, Long> {

    /**
     * 특정 (핀, 사용자)에 대한 레코드 존재 여부 확인
     *
     * @param pinId 핀 ID
     * @param userId 사용자 ID
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByPinIdAndUserId(Long pinId, Long userId);

    /**
     * 특정 핀에 대해 특정 사용자의 좋아요 엔티티 조회
     *
     * @param pinId 핀 ID
     * @param userId 사용자 ID
     * @return 좋아요 엔티티가 존재하면 Optional에 담아 반환, 없으면 빈 Optional 반환
     */
    Optional<Likes> findByPinIdAndUserId(Long pinId, Long userId);

    /**
     * 특정 핀에 대한 좋아요 수 조회
     *
     * @param pinId 핀 ID
     * @return 좋아요 수
     */
    long countByPinId(Long pinId);
}
