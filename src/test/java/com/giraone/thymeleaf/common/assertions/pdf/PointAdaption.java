package com.giraone.thymeleaf.common.assertions.pdf;

public record PointAdaption(
    float x,
    float y) {

    @Override
    public String toString() {
        return "PointAdaption{" +
            "x=" + x +
            ", y=" + y +
            '}';
    }
}
