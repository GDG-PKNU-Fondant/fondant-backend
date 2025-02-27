package com.fondant.wishlist.domain.repository;

import com.fondant.wishlist.domain.entity.WishListEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishListRepository extends JpaRepository<WishListEntity,Long> {
    Page<WishListEntity> findByUserId(Long userId, Pageable pageable);
}
