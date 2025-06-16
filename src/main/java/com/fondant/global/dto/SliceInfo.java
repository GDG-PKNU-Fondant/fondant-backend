package com.fondant.global.dto;

public record SliceInfo(
        boolean hasNext
) {
    public static SliceInfo of(boolean hasNext) {
        return new SliceInfo(hasNext);
    }
}