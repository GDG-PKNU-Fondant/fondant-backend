package com.fondant.payment.domain.entity;

import com.fondant.order.domain.entity.OrderEntity;

import com.fondant.order.presentation.dto.request.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @NotNull
    private OrderEntity order;

    @Column
    @NotNull
    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column
    @Getter
    @NotNull
    private String method;

    @Column
    private String paymentId;

    @Column
    private String failReason;

    @Column
    @NotNull
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime paidAt;

    @Builder
    public PaymentEntity (Double amount, PaymentStatus status,
                          String method, String paymentId,
                          String failReason, LocalDateTime paidAt,
                          OrderEntity order
    ) {
        this.amount = amount;
        this.status = status;
        this.method = method;
        this.paymentId = paymentId;
        this.failReason = failReason;
        this.createdAt = LocalDateTime.now();
        this.paidAt = paidAt;
        this.order = order;
    }

    public void markSuccess() {
        this.status = PaymentStatus.SUCCESS;
        this.paidAt = LocalDateTime.now();
    }

    public void markFail(String failReason) {
        this.status = PaymentStatus.FAILED;
        this.failReason = failReason;
    }
}
