package com.fondant.test;

import lombok.Builder;

@Builder
public record TestDto(
        String name,
        int age
) {
    public static TestDto of(String name, int age) {
        return TestDto.builder().name(name).age(age).build();
    }
}
