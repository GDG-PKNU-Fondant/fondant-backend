package com.fondant.test.repository;

import com.fondant.user.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTestRepository extends JpaRepository<UserEntity,Long> {
    UserEntity findByEmail(String email);
}
