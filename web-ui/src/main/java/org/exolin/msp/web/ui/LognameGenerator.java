package org.exolin.msp.web.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 *
 * @author tomgk
 */
public class LognameGenerator
{
    private static final DateTimeFormatter FORMAT = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral("-")
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral("-")
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral("-")
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .toFormatter();
    
    public static String generateFilename(String group, LocalDateTime dateTime)
    {
        return group+"-"+FORMAT.format(dateTime)+".log";
    }

    public static String getPrefix(String group)
    {
        return group+"-";
    }

    public static String creatTaskGroup(String taskName)
    {
        return "task-"+taskName;
    }
}
