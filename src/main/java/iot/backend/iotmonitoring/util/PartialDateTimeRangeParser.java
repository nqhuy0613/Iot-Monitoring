package iot.backend.iotmonitoring.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.regex.Pattern;

public final class PartialDateTimeRangeParser {

    private static final Pattern YEAR_PATTERN = Pattern.compile("^\\d{4}$");
    private static final Pattern MONTH_PATTERN = Pattern.compile("^\\d{4}-\\d{2}$");
    private static final Pattern DAY_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern HOUR_PATTERN = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2} \\d{2}$"
    );
    private static final Pattern MINUTE_PATTERN = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$"
    );
    private static final Pattern SECOND_PATTERN = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$"
    );

    private static final DateTimeFormatter YEAR_FORMATTER = strictFormatter("uuuu");
    private static final DateTimeFormatter MONTH_FORMATTER = strictFormatter("uuuu-MM");
    private static final DateTimeFormatter DAY_FORMATTER = strictFormatter("uuuu-MM-dd");
    private static final DateTimeFormatter HOUR_FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendPattern("uuuu-MM-dd HH")
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter MINUTE_FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendPattern("uuuu-MM-dd HH:mm")
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter SECOND_FORMATTER = strictFormatter(
            "uuuu-MM-dd HH:mm:ss"
    );

    private PartialDateTimeRangeParser() {
    }

    public static DateTimeRange parse(String rawValue) {
        String value = rawValue == null ? null : rawValue.trim();

        try {
            if (value != null && YEAR_PATTERN.matcher(value).matches()) {
                LocalDateTime start = Year.parse(value, YEAR_FORMATTER)
                        .atDay(1)
                        .atStartOfDay();
                return new DateTimeRange(start, start.plusYears(1));
            }

            if (value != null && MONTH_PATTERN.matcher(value).matches()) {
                LocalDateTime start = YearMonth.parse(value, MONTH_FORMATTER)
                        .atDay(1)
                        .atStartOfDay();
                return new DateTimeRange(start, start.plusMonths(1));
            }

            if (value != null && DAY_PATTERN.matcher(value).matches()) {
                LocalDateTime start = LocalDate.parse(value, DAY_FORMATTER).atStartOfDay();
                return new DateTimeRange(start, start.plusDays(1));
            }

            if (value != null && HOUR_PATTERN.matcher(value).matches()) {
                LocalDateTime start = LocalDateTime.parse(value, HOUR_FORMATTER);
                return new DateTimeRange(start, start.plusHours(1));
            }

            if (value != null && MINUTE_PATTERN.matcher(value).matches()) {
                LocalDateTime start = LocalDateTime.parse(value, MINUTE_FORMATTER);
                return new DateTimeRange(start, start.plusMinutes(1));
            }

            if (value != null && SECOND_PATTERN.matcher(value).matches()) {
                LocalDateTime start = LocalDateTime.parse(value, SECOND_FORMATTER);
                return new DateTimeRange(start, start.plusSeconds(1));
            }
        } catch (DateTimeParseException exception) {
            throw invalidTimeFormat();
        }

        throw invalidTimeFormat();
    }

    private static DateTimeFormatter strictFormatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern)
                .withResolverStyle(ResolverStyle.STRICT);
    }

    private static ResponseStatusException invalidTimeFormat() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "time must use one of: yyyy, yyyy-MM, yyyy-MM-dd, "
                        + "yyyy-MM-dd HH, yyyy-MM-dd HH:mm, yyyy-MM-dd HH:mm:ss"
        );
    }

    public record DateTimeRange(
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    ) {
    }
}
