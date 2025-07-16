package com.fondant.product.util;

import com.fondant.global.exception.ApiException;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.product.exception.ProductError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductUtil {

    private final ProductRepository productRepository;

    @Transactional
    public void reserveStock(Long productId, Integer quantity) {
        ProductEntity product = productRepository.findByIdWithPessimisticLock(productId);
        if (product.getMaxCount() < quantity) throw new ApiException(ProductError.OUT_OF_STOCK);
        product.decreaseStock(quantity);
    }

    @Transactional
    public void rollbackReservedStockWithLock(Long productId, Integer quantity) {
        ProductEntity product = productRepository.findByIdWithPessimisticLock(productId);
        product.increaseStock(quantity);
    }
}
