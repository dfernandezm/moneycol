package com.moneycol.indexer.indexing.index;

import lombok.Builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
public class DateUtil {

    public LocalDateTime getTodayDate() {
        return LocalDateTime.now();
    }

    public String getTodayDateAsString() {
        return getTodayDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
}
