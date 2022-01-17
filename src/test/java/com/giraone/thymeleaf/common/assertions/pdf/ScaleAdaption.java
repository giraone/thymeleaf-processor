package com.giraone.thymeleaf.common.assertions.pdf;

public record ScaleAdaption (
    float xScale,
    float yScale
) {
    @Override
    public String toString() {
        return "ScaleAdaption{" +
            "xScale=" + xScale +
            ", yScale=" + yScale +
            '}';
    }
}
