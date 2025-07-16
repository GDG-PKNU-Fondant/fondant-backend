package com.fondant.payment.application;

import com.fondant.coupon.application.CouponService;
import com.fondant.coupon.domain.repository.UserCouponRepository;
import com.fondant.order.application.OrderService;
import com.fondant.order.domain.entity.OrderDetailEntity;
import com.fondant.order.domain.repository.OrderDetailRepository;
import com.fondant.order.presentation.dto.CouponApplyDto;
import com.fondant.infra.portone.util.PortOneApiClient;
import com.fondant.order.domain.entity.OrderEntity;
import com.fondant.order.domain.repository.OrderRepository;
import com.fondant.order.exception.OrderError;
import com.fondant.payment.application.dto.PaymentDetails;
import com.fondant.payment.domain.entity.PaymentEntity;
import com.fondant.payment.domain.entity.PaymentStatus;
import com.fondant.payment.domain.repository.PaymentRepository;
import com.fondant.global.exception.ApiException;
import com.fondant.payment.exception.PaymentError;
import com.fondant.payment.presentation.dto.PaymentRequest;
import com.fondant.payment.presentation.dto.PaymentResponse;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.exception.ProductError;
import com.fondant.user.application.UserService;
import com.fondant.user.application.dto.CustomUserDetails;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.exception.UserError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PortOneApiClient portOneApiClient;
    private final OrderDetailRepository orderDetailRepository;
    private final UserService userService;
    private final OrderService orderService;
    private final CouponService couponService;

    @Transactional
    public PaymentResponse completePayment(CustomUserDetails userDetails, PaymentRequest request) {

        UserEntity userEntity = userService.getUserEntityById(userDetails.getUserId());

        PaymentDetails paymentDetails;
        try {
            paymentDetails = portOneApiClient.getPaymentDetails(request.paymentId());
            log.info("결제 정보 조회 성공: {}", request.paymentId());
        } catch (Exception e) {
            log.error("결제 정보 조회 실패", e);
            throw new ApiException(PaymentError.GET_PAYMENT_FAILED);
        }

        OrderEntity order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ApiException(OrderError.ORDER_NOT_FOUND));

        if (!Objects.equals(order.getUser().getId(), userEntity.getId())) {
            throw new ApiException(OrderError.ORDER_NOT_FOUND);
        }

        if (!Objects.equals(Double.valueOf(paymentDetails.totalAmount()), order.getTotalPrice())) {
            PaymentEntity payment = createPayment(paymentDetails, PaymentStatus.FAILED, "결제 금액 불일치");
            paymentRepository.save(payment);
            throw new ApiException(PaymentError.PAYMENT_AMOUNT_MISMATCH);
        }

        PaymentEntity payment = createPayment(paymentDetails, PaymentStatus.SUCCESS, null);
        paymentRepository.save(payment);

        order.markPaid();

        List<Long> usedCouponIds = paymentDetails.orderDetails().couponAllies()
                .stream().map(CouponApplyDto::couponId).toList();
        Integer usedPoint = paymentDetails.orderDetails().discountPoints();

        finalizeOrderAfterPayment(
                order.getId(),
                payment.getId(),
                usedCouponIds,
                usedPoint
        );

        return PaymentResponse.builder()
                .paymentMethod(payment.getMethod())
                .status(String.valueOf(payment.getStatus()))
                .amount(payment.getAmount().intValue())
                .build();
    }

    @Transactional
    public void cancelPayment(String paymentId) {
        PaymentEntity payment = paymentRepository.findByPaymentId(paymentId);
        OrderEntity order = payment.getOrder();

        try {
            portOneApiClient.cancelPayment(paymentId);
            orderService.rollbackOrderAndStock(order.getId());
            order.markCanceled();
            log.info("포트원 결제 취소 성공");
        } catch (Exception e) {
            log.error("자동 환불 실패", e);
            throw new ApiException(PaymentError.REFUND_FAILED);
        }

        payment.markFail("고객 요청으로 인한 취소");
    }

    public void finalizeOrderAfterPayment(Long orderId, Long paymentId, List<Long> usedCouponIds, Integer usedPoint) {

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(OrderError.ORDER_NOT_FOUND));

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ApiException(PaymentError.PAYMENT_NOT_FOUND));

        if (usedPoint > 0) {
            UserEntity user = order.getUser();
            if (user.getPoint() < usedPoint) {
                throw new ApiException(UserError.INVALID_POINT);
            }
            user.changePoint(usedPoint);
        }

        if (usedCouponIds != null && !usedCouponIds.isEmpty()) {
            couponService.useCoupons(usedCouponIds);
        }

        List<OrderDetailEntity> orderDetails = orderDetailRepository.findAllByOrder(order);
        for (OrderDetailEntity detail : orderDetails) {
            ProductEntity product = detail.getProduct();
            int newStock = product.getMaxCount() - detail.getQuantity();
            if (newStock < 0) {
                throw new ApiException(ProductError.OUT_OF_STOCK);
            }
            product.decreaseStock(detail.getQuantity());
        }

        payment.markSuccess();
    }

    private PaymentEntity createPayment(PaymentDetails details, PaymentStatus status, String failReason) {
        return PaymentEntity.builder()
                .amount(Double.valueOf(details.totalAmount()))
                .method(details.method())
                .paymentId(details.paymentId())
                .status(status)
                .failReason(failReason)
                .paidAt(LocalDateTime.now())
                .build();
    }
}