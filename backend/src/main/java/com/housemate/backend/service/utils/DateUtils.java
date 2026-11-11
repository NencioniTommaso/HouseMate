package com.housemate.backend.service.utils;

import java.util.Date;

public class DateUtils {
    public static boolean isDatePassed(Date date) {
        return date.before(new Date(System.currentTimeMillis()));
    }
}
