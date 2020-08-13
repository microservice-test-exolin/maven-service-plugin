package org.exolin.msp.web.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;

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
    
    public static String getLogFileTitle(String filename)
    {
        if(filename.equals("service.log"))
            return "Service Log";
        else if(filename.matches(".*-....-..-..-......\\.log"))
        {
            int suffixLen = 22;
            int extLen = 4;  //.log
            
            String group = filename.substring(0, filename.length()-suffixLen);
            
            String ts = filename.substring(filename.length()-suffixLen+1, filename.length()-extLen);
            System.out.println(ts);
            
            TemporalAccessor parse = FORMAT.parse(ts);
            LocalDateTime dateTime = parse.query(TemporalQueries.localDate()).atTime(parse.query(TemporalQueries.localTime()));
            
            if(group.startsWith("task-"))
                group = "Task "+group.substring(5);
            
            return group+" at "+dateTime.toString().replace('T', ' ');
        }
        else
            return filename;
    }
}
