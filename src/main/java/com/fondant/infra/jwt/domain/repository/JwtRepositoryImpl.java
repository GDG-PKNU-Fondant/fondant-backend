package com.fondant.infra.jwt.domain.repository;

import com.fondant.infra.jwt.domain.entity.QRefreshEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class JwtRepositoryImpl implements JwtRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public void deleteByExpiresBefore(LocalDateTime now) {
        QRefreshEntity refresh = QRefreshEntity.refreshEntity;

        try {
            queryFactory.delete(refresh)
                    .where(refresh.expires.before(now))
                    .execute();
        } catch (Exception e) {
            throw new RuntimeException("만료된 리프레쉬 토큰을 제거하는데 실패했습니다.");
        }
    }
}
