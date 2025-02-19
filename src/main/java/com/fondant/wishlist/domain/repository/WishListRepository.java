package com.fondant.wishlist.domain.repository;

import com.fondant.wishlist.domain.entity.WishListEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishListRepository extends JpaRepository<WishListEntity,Long> {
}
