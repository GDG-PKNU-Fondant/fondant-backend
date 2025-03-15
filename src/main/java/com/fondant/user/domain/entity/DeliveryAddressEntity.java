package com.fondant.user.domain.entity;

import com.fondant.user.presentation.dto.request.DeliveryAddressUpdateRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "delivery_address")
@Getter
public class DeliveryAddressEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_address_id")
    private Long id;

    @NotNull
    @Column(name="delivery_address")
    private String deliveryAddress;

    @NotNull
    @Column(name="is_primary")
    private Boolean isPrimary;

    @NotNull
    @Column(name="post_code")
    private String postCode;

    @Column(name="alias")
    private String alias;

    @NotNull
    @Column(name="receiver_name")
    private String receiverName;

    @NotNull
    @Column(name="receiver_phone_number")
    private String receiverPhoneNumber;

    @Builder(toBuilder = true)
    public DeliveryAddressEntity(String deliveryAddress, Boolean isPrimary, String postCode, String alias, String receiverName, String receiverPhoneNumber){
        this.deliveryAddress = deliveryAddress;
        this.isPrimary = isPrimary;
        this.postCode = postCode;
        this.alias = alias;
        this.receiverName = receiverName;
        this.receiverPhoneNumber = receiverPhoneNumber;
    }

    public void updateIsPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public void update(DeliveryAddressUpdateRequest request) {
        this.deliveryAddress = request.deliveryAddress();
        this.postCode = request.postCode();
        this.alias = request.alias();
        this.receiverName = request.receiverName();
        this.receiverPhoneNumber = request.receiverPhoneNumber();
        this.isPrimary = request.isPrimary();
    }
}
