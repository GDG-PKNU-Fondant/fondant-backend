package com.fondant.test.repository;

import com.fondant.infra.jwt.domain.entity.RefreshEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTestRepository extends JpaRepository<RefreshEntity,Long> {
}
