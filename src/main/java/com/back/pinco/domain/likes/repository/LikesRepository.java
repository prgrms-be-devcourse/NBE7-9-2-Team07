package com.back.pinco.domain.likes.repository;

import com.back.pinco.domain.likes.entity.Likes;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<Likes, Long> {

    /**
     * 특정 (핀, 사용자)에 대한 레코드 존재 여부 확인
     *
     * @param pinId  핀 ID
     * @param userId 사용자 ID
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByPin_IdAndUser_Id(Long pinId, Long userId);

    /**
     * 특정 핀에 대해 특정 사용자의 좋아요 엔티티 조회
     *
     * @param pinId  핀 ID
     * @param userId 사용자 ID
     * @return 좋아요 엔티티가 존재하면 Optional에 담아 반환, 없으면 빈 Optional 반환
     */
    @Query("SELECT l FROM Likes l WHERE l.pin.id = :pinId AND l.user.id = :userId")
    Optional<Likes> findByPinIdAndUserId(@Param("pinId") Long pinId, @Param("userId") Long userId);

    /**
     * 특정 핀에 대한 좋아요 수 조회
     *
     * @param pinId 핀 ID
     * @return 좋아요 수
     */
    long countByPin_Id(Long pinId);

    /**
     * 특정 핀에 대한 좋아요 존재 여부 확인
     *
     * @param pinId
     * @return boolean
     */
    boolean existsByPin_Id(Long pinId);

    /**
     * 특정 핀에 좋아요를 누른 모든 사용자 조회
     *
     * @param pinId 핀 ID
     * @return 사용자들 정보
     */
    @Query("SELECT DISTINCT l.user FROM Likes l WHERE l.pin.id = :pinId")
    List<User> findUsersByPinId(@Param("pinId") Long pinId);

    /**
     * 특정 사용자가 좋아요한 모든 핀 엔티티 조회
     *
     * @param userId 사용자 ID
     * @return 핀들 정보
     */
    @Query("SELECT DISTINCT l.pin FROM Likes l WHERE l.user.id = :userId")
    List<Pin> findPinsByUserId(@Param("userId") Long userId);
}
