package com.back.pinco.domain.pin.repository;

import com.back.pinco.domain.pin.entity.Pin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PinRepository extends JpaRepository<Pin,Long> {
}
