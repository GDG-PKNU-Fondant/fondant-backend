package com.fondant.user.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "delivery_address")
public class DeliveryAddressEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_address_id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @NotNull
    @Column(name="delivery_address")
    private String deliveryAddress;

    @NotNull
    @Column(name="is_primary")
    private Boolean primary;

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

    @Builder
    public DeliveryAddressEntity(Long id, UserEntity user, String deliveryAddress, Boolean isPrimary, String postCode, String alias, String receiverName, String receiverPhoneNumber){
        this.id = id;
        this.user = user;
        this.deliveryAddress = deliveryAddress;
        this.primary = isPrimary;
        this.postCode = postCode;
        this.alias = alias;
        this.receiverName = receiverName;
        this.receiverPhoneNumber = receiverPhoneNumber;
    }
}
