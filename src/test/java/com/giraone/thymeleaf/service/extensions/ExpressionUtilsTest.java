package com.giraone.thymeleaf.service.extensions;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpressionUtilsTest {

    @ParameterizedTest
    @CsvSource(value = {
        "null,''", // null => empty
        "'',''", // empty => empty
        "2019-01-10,10.01.2019",
        "20aa-01-01,E-20aa-01-01", // invalid => E prefixed input
    }, nullValues = "null")
    public void formatGermanDDMMYYYY(String input, String expectedOutput) {

        // act
        String result = ExpressionUtils.formatGermanDDMMYYYY(input);

        // assert
        assertThat(result).isEqualTo(expectedOutput);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "null,''", // null => empty
        "'',''", // empty => empty
        "2019-01-10,Januar 2019",
        "20aa-01-01,E-20aa-01-01", // invalid => E prefixed input
    }, nullValues = "null")
    public void formatGermanMonthYear(String input, String expectedOutput) {

        // act
        String result = ExpressionUtils.formatGermanMonthYear(input);
        // assert
        assertThat(result).isEqualTo(expectedOutput);
    }
}