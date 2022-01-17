package com.giraone.thymeleaf.common.assertions.pdf;

/**
 * Specification for a rectangle to be used in testing PDF output.
 * @param leftUpperX Upper left position horizontally from left in mm
 * @param leftUpperY Upper left position vertically from top in mm
 * @param rightLowerX Lower right position horizontally from left in mm
 * @param rightLowerY Lower right position vertically from top in mm
 */
public record RectangleInMm(
    float leftUpperX,
    float leftUpperY,
    float rightLowerX,
    float rightLowerY
) {

    @Override
    public String toString() {
        return String.format("[%5.1f, %5.1f] - [%5.1f, %5.1f]", leftUpperX, leftUpperY, rightLowerX, rightLowerY);
    }
}
