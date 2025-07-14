package com.fondant.order.domain.repository;

import com.fondant.order.domain.entity.OrderDetailEntity;
import com.fondant.order.domain.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetailEntity, Long> {
    List<OrderDetailEntity> findAllByOrder(OrderEntity order);
}
