package org.exolin.msp.web.ui;

import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 * Tests für {@link LognameGenerator}.
 *
 * @author tomgk
 */
public class LognameGeneratorTest
{
    private static final String SERVICE_NAME = "xyz-abc";
    
    @Test
    public void testGetLogFileTitle_Service_New()
    {
        assertEquals("Service Log", LognameGenerator.getLogFileTitle(SERVICE_NAME, "service.log"));
    }
    
    @Test
    public void testGetLogFileTitle_Service_Old()
    {
        assertEquals("Service Log [old version]", LognameGenerator.getLogFileTitle(SERVICE_NAME, SERVICE_NAME+".log"));
    }
    
    @Test
    public void testGetLogFileTitle_GenericGroup()
    {
        assertEquals("x at 2020-01-02 03:04:05", LognameGenerator.getLogFileTitle(SERVICE_NAME, "x-2020-01-02-030405.log"));
    }
    
    @Test
    public void testGetLogFileTitle_Task()
    {
        assertEquals("Task x at 2020-01-02 03:04:05", LognameGenerator.getLogFileTitle(SERVICE_NAME, "task-x-2020-01-02-030405.log"));
    }
    
    @Test
    public void testGetLogFileTitle_Other()
    {
        assertEquals("abc.log", LognameGenerator.getLogFileTitle(SERVICE_NAME, "abc.log"));
    }
    
    @Test
    public void testGetLogFileTitle2_Service_New()
    {
        assertEquals("Service Log", LognameGenerator.getLogFileTitle(SERVICE_NAME, Optional.empty(), Paths.get("service.log")));
    }
    
    @Test
    public void testGetLogFileTitle2_Service_New_Timed()
    {
        assertEquals("Service Log 2020-08-16-00-07", LognameGenerator.getLogFileTitle(SERVICE_NAME, Optional.empty(), Paths.get("service.2020-08-16-00-07.log")));
    }
    
    @Test
    public void testGetLogFileTitle2_Service_Old()
    {
        assertEquals("Service Log [old version]", LognameGenerator.getLogFileTitle(SERVICE_NAME, Optional.empty(), Paths.get(SERVICE_NAME+".log")));
    }
    
    @Test
    public void testGetLogFileTitle2_GenericGroup()
    {
        assertEquals("Task x at 2020-01-02 03:04:05", LognameGenerator.x("x-2020-01-02-030405.log", Optional.of("x")));
        assertEquals("Task x at 2020-01-02 03:04:05", LognameGenerator.getLogFileTitle(SERVICE_NAME, Optional.of("x"), Paths.get("x-2020-01-02-030405.log")));
    }
    
    @Test
    public void testGetLogFileTitle2_Task_old()
    {
        assertEquals("Task x at 2020-01-02 03:04:05", LognameGenerator.getLogFileTitle(SERVICE_NAME, Optional.of("x"), Paths.get("task-x-2020-01-02-030405.log")));
    }
    
    @Test
    public void testGetLogFileTitle2_TaskNew()
    {
        Instant i = LocalDateTime.of(2020,01,02,03,04,05).atZone(ZoneId.systemDefault()).toInstant();
        assertEquals("Task x at 2020-01-02 03:04:05", LognameGenerator.getLogFileTitle(SERVICE_NAME, Optional.of("x"), Paths.get(i.toEpochMilli()+".log")));
    }
    
    @Test
    public void testGetLogFileTitle2_Other()
    {
        assertEquals("abc.log", LognameGenerator.getLogFileTitle(SERVICE_NAME, Optional.empty(), Paths.get("abc.log")));
        assertEquals("Task x: abc.log", LognameGenerator.getLogFileTitle(SERVICE_NAME, Optional.of("x"), Paths.get("abc.log")));
    }
}
