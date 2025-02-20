package com.fondant.product.domain.repository;

import com.fondant.product.domain.entity.OptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;

public interface OptionRepository extends JpaRepository<OptionEntity, Long> {
    List<OptionEntity> findByProductId(Long productId);
}
