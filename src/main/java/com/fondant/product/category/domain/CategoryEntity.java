package com.fondant.product.category.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
    @Getter
    private String name;

    @Column(name="icon_url")
    @Getter
    private String iconUrl;

    @Getter
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private CategoryEntity parent;

    @OneToMany(mappedBy = "parent",cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    private List<CategoryEntity> children = new ArrayList<>();;

    @Builder
    public CategoryEntity(String name,String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
    }

    public void addChild(CategoryEntity child) {
        this.children.add(child);
        child.parent = this;
    }
}
