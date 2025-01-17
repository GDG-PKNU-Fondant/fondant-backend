package com.fondant.user.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="account")
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<DeliveryAddressEntity> deliveryAddresses = new ArrayList<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="sns_type")
    private SNSType snsType;

    @NotNull
    @Column(name="name")
    private String name;

    @NotNull
    @Column(name="phone_number")
    private String phoneNumber;

    @NotNull
    @Column(name="email")
    private String email;

    @Column(name="birth")
    private Date birth;

    @Column(name="nickname")
    private String nickname;

    @Column(name="profile_url")
    private String profileUrl;

    @NotNull
    @Column(name="create_at", updatable = false)
    private LocalDate createAt;

    @NotNull
    @Column(name="gender")
    private Gender gender;

    @Builder
    public UserEntity(Long id, SNSType snsType, String name, String phoneNumber, String email, Date birth, String nickname, String profileUrl, LocalDate createAt, Gender gender) {
        this.id = id;
        this.snsType = snsType;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.birth = birth;
        this.nickname = nickname;
        this.profileUrl = profileUrl;
        this.createAt = createAt;
        this.gender = gender;
    }
}
