package com.fondant.user.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@Getter
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    @Getter
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<DeliveryAddressEntity> deliveryAddresses = new ArrayList<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sns_type")
    private SNSType snsType;

    @NotNull
    @Column(name = "name")
    private String name;

    @Column(name = "password")
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "verified_phone")
    private boolean verifiedPhone;

    @NotNull
    @Column(name = "email")
    private String email;

    @Column(name = "birth")
    private Date birth;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "profile_url")
    private String profileUrl;

    @NotNull
    @Column(name = "create_at")
    private LocalDate createAt;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotNull
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @NotNull
    private int point;

    @Builder(toBuilder = true)
    public UserEntity(
            Long id, SNSType snsType, String name, String phoneNumber,
            boolean verifiedPhone, String email,
            String password, Date birth, String nickname,
            String profileUrl, LocalDate createAt, Gender gender,
            UserRole role, int point) {
        this.id = id;
        this.snsType = snsType;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.verifiedPhone = verifiedPhone;
        this.email = email;
        this.password = password;
        this.birth = birth;
        this.nickname = nickname;
        this.profileUrl = profileUrl;
        this.createAt = createAt;
        this.gender = gender;
        this.role = role;
        this.point = point;
    }

    public void changePoint(int newPoint) {
        this.toBuilder()
                .point(newPoint)
                .build();
    }
}
