package com.fondant.product.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="category")
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="category_id")
    @Getter
    private Long id;

    @NotNull
    @Column(name="name")
    private String name;

    @Builder
    public CategoryEntity(String name) {
        this.name = name;
    }
}
