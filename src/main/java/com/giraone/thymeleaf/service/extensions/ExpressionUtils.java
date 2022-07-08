package com.giraone.thymeleaf.service.extensions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Some examples for usefull expressions for date handling.
 */
public final class ExpressionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionUtils.class);

    private static final DateTimeFormatter FORMATTER_GERMAN_DDMMYYYY = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter FORMATTER_GERMAN_MONTH_YEAR = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.GERMAN);

    // Hide
    private ExpressionUtils() {
    }

    /**
     * Format a german date as dd.MM.yyyy, when the input is an ISO date string.
     * @param input date as ISO string
     * @return date string in german notation dd.MM.yyyy
     */
    public static String formatGermanDDMMYYYY(String input) {

        if (input == null || input.trim().length() == 0) {
            return "";
        } else {
            try {
                return FORMATTER_GERMAN_DDMMYYYY.format(DateTimeFormatter.ISO_DATE.parse(input));
            } catch (Exception e) {
                LOGGER.error("error while formatting to german date", e);
                return "E-" + input;
            }
        }
    }

    public static String formatGermanMonthYear(String input) {

        if (input == null || input.trim().length() == 0) {
            return "";
        } else {
            try {
                return FORMATTER_GERMAN_MONTH_YEAR.format(DateTimeFormatter.ISO_DATE.parse(input));
            } catch (Exception e) {
                LOGGER.error("error while formatting to german month/year", e);
                return "E-" + input;
            }
        }
    }
}