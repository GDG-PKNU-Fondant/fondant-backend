package com.fondant.coupon.domain.repository;

import com.fondant.coupon.domain.entity.CouponEntity;
import com.fondant.coupon.domain.entity.QCouponEntity;
import com.fondant.coupon.domain.entity.QCouponMarketEntity;
import com.fondant.coupon.domain.entity.QUserCouponEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<CouponEntity> findIssuableCoupons(Long userId, Pageable pageable) {
        QCouponEntity coupon = QCouponEntity.couponEntity;
        QCouponMarketEntity couponMarket = QCouponMarketEntity.couponMarketEntity;
        QUserCouponEntity userCoupon = QUserCouponEntity.userCouponEntity;

        BooleanBuilder builder = new BooleanBuilder();
        LocalDateTime now = LocalDateTime.now();

        builder.and(coupon.startDate.loe(now));
        builder.and(coupon.endDate.goe(now));
        builder.and(userCoupon.id.isNull());

        List<CouponEntity> content = queryFactory
                .selectDistinct(coupon)
                .from(coupon)
                .leftJoin(coupon.couponMarkets, couponMarket)
                .leftJoin(userCoupon)
                .on(userCoupon.coupon.eq(coupon)
                        .and(userCoupon.user.id.eq(userId)))
                .where(builder)
                .orderBy(
                        coupon.endDate.asc(),
                        coupon.id.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            content.remove(pageable.getPageSize());
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Slice<CouponEntity> findIssuedCoupons(Long userId, Pageable pageable) {

        QCouponEntity coupon = QCouponEntity.couponEntity;
        QUserCouponEntity userCoupon = QUserCouponEntity.userCouponEntity;
        LocalDateTime now = LocalDateTime.now();

        BooleanBuilder builder = new BooleanBuilder()
                .and(coupon.startDate.loe(now))
                .and(coupon.endDate.goe(now))
                .and(userCoupon.user.id.eq(userId))
                .and(userCoupon.isUsed.isFalse());

        List<CouponEntity> content = queryFactory
                .select(coupon)
                .from(coupon)
                .join(userCoupon).on(userCoupon.coupon.eq(coupon))
                .where(builder)
                .orderBy(coupon.endDate.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            content.remove(pageable.getPageSize());
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }
}
