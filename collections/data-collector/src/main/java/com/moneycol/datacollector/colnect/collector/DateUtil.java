package com.moneycol.datacollector.colnect.collector;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter basicDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static String dateOfToday() {
       return LocalDateTime.now().format(basicDateFormatter);
    }
}
