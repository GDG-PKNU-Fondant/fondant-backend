package com.fondant.infra.jwt.domain.repository;

import java.time.LocalDateTime;

public interface JwtRepositoryCustom {
    void deleteByExpiresBefore(LocalDateTime now);
}
