package com.giraone.thymeleaf.common.assertions.pdf;

public record TextLocation(
    String text,
    int page,
    float x,
    float y,
    float width,
    float height
) {
    public float getRightLowerX() {
        return x + width;
    }

    public float getRightLowerY() {
        return y + height;
    }

    public String printPos() {
        return String.format("p%d [%5.1f, %5.1f] - [%5.1f, %5.1f]", page, x, y, getRightLowerX(), getRightLowerY());
    }
}
