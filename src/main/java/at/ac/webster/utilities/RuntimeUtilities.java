package at.ac.webster.utilities;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public final class RuntimeUtilities {
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static LocalDate convertToLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
