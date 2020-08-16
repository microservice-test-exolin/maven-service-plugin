package org.exolin.msp.web.ui;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Optional;
import org.exolin.msp.service.LogFile;

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
    
    public static String getLogFileTitle(String serviceName, String filename)
    {
        if(filename.equals("service.log"))
            return "Service Log";
        else if(filename.equals("service.out.log"))
            return "Service Output";
        
        String title;
        title = x(filename, Optional.empty());
        if(title != null)
            return title;
        
        //----------------------------------------------------------------------
        //Backwards compatibliity start
        //----------------------------------------------------------------------
        title = getOldServiceLogFileTitle(serviceName, filename);
        if(title != null)
            return title;
        title = getOldProcessLogFileTitle(filename);
        if(title != null)
            return title;
        //----------------------------------------------------------------------
        //Backwards compatibliity end
        //----------------------------------------------------------------------
        return filename;
    }
    
    public static String getLogFileTitle(LogFile logFile)
    {
        String filename = logFile.getPath().getFileName().toString();
        
        if(logFile.getProcessName().isPresent())
        {
            String title;
            title = getOldProcessLogFileTitle(filename);
            if(title != null)
                return title;
            title = x(filename, logFile.getProcessName());
            if(title != null)
                return title;
            
            if(filename.endsWith(".log"))
            {
                if(filename.endsWith(".log"))
                {
                    String ts = filename.substring(0, filename.length()-4);
                    try{
                        long timeMillis = Long.parseLong(ts);
                        return "Task "+logFile.getProcessName().get()+" at "+Instant.ofEpochMilli(timeMillis).atZone(ZoneId.systemDefault()).toLocalDateTime().toString().replace('T', ' ');
                    }catch(NumberFormatException e){
                    }
                }
            }
            
            return "Task "+logFile.getProcessName().get()+": "+logFile.getPath().getFileName();
        }
        else
        {
            String title = getOldServiceLogFileTitle(logFile.getServiceName(), filename);
            if(title != null)
                return title;
            else if(filename.equals("service.log"))
                return "Service Log";
            else if(filename.startsWith("service.") && filename.endsWith(".log"))
                return "Service Log "+filename.substring("service.".length(), filename.length()-".log".length());
            
            return filename;
        }
    }
    
    static String x(String filename, Optional<String> expectedTaskName)
    {
        if(!filename.matches(".*-....-..-..-......\\.log"))
            return null;
        
        int suffixLen = 22;
        int extLen = 4;  //.log

        String group = filename.substring(0, filename.length()-suffixLen);

        String ts = filename.substring(filename.length()-suffixLen+1, filename.length()-extLen);

        TemporalAccessor parse = FORMAT.parse(ts);
        LocalDateTime dateTime = parse.query(TemporalQueries.localDate()).atTime(parse.query(TemporalQueries.localTime()));

        if(group.startsWith("task-"))
        {
            String taskName = group.substring(5);
            
            if(expectedTaskName.map(e -> e.equals(taskName)).orElse(true))
                group = "Task "+taskName;
            else
                group = "Task "+expectedTaskName.get();
        }
        else if(expectedTaskName.isPresent())
            group = "Task "+expectedTaskName.get();

        return group+" at "+dateTime.toString().replace('T', ' ');
    }
    
    //----------------------------------------------------------------------
    //Backwards compatibliity functionality
    //----------------------------------------------------------------------
    private static String getOldServiceLogFileTitle(String serviceName, String filename)
    {
        if(filename.equals(serviceName+".log"))
            return "Service Log [old version]";
        if(filename.equals(serviceName+".error.log"))
            return "Service Error Log [old version]";
        else
            return null;
    }
    
    static String getOldProcessLogFileTitle(String filename)
    {
        if(filename.equals("build.out.log"))
            return "Build Log";
        else if(filename.equals("build.err.log"))
            return "Build Error Log";
        else if(filename.equals("deploy.out.log"))
            return "Deploy Log";
        else if(filename.equals("deploy.err.log"))
            return "Deploy Error Log";
        else
            return null;
    }
}
