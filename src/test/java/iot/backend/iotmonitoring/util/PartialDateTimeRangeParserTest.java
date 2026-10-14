package iot.backend.iotmonitoring.util;

import iot.backend.iotmonitoring.util.PartialDateTimeRangeParser.DateTimeRange;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartialDateTimeRangeParserTest {

    @Test
    void createsRangeForEverySupportedPrecision() {
        assertRange("2026", "2026-01-01T00:00:00", "2027-01-01T00:00:00");
        assertRange("2026-08", "2026-08-01T00:00:00", "2026-09-01T00:00:00");
        assertRange("2026-08-15", "2026-08-15T00:00:00", "2026-08-16T00:00:00");
        assertRange("2026-08-15 11", "2026-08-15T11:00:00", "2026-08-15T12:00:00");
        assertRange("2026-08-15 11:20", "2026-08-15T11:20:00", "2026-08-15T11:21:00");
        assertRange(
                "2026-08-15 11:20:30",
                "2026-08-15T11:20:30",
                "2026-08-15T11:20:31"
        );
    }

    @Test
    void rejectsInvalidDateOrUnsupportedFormat() {
        assertThrows(
                ResponseStatusException.class,
                () -> PartialDateTimeRangeParser.parse("2026-13")
        );
        assertThrows(
                ResponseStatusException.class,
                () -> PartialDateTimeRangeParser.parse("2026-02-30")
        );
        assertThrows(
                ResponseStatusException.class,
                () -> PartialDateTimeRangeParser.parse("2026-08-15T11:20:30")
        );
    }

    private void assertRange(String input, String expectedStart, String expectedEnd) {
        DateTimeRange range = PartialDateTimeRangeParser.parse(input);
        assertEquals(LocalDateTime.parse(expectedStart), range.startInclusive());
        assertEquals(LocalDateTime.parse(expectedEnd), range.endExclusive());
    }
}
