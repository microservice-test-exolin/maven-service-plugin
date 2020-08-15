package org.exolin.msp.web.ui;

import java.time.LocalDateTime;
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
    public void testGenerateFilename()
    {
        assertEquals("deploy-2020-01-02-030405.log", LognameGenerator.generateFilename("deploy", LocalDateTime.of(2020, 1, 2, 3, 4, 5)));
    }
    
    @Test
    public void testGetLogFileTitle_Service()
    {
        assertEquals("Service Log", LognameGenerator.getLogFileTitle(SERVICE_NAME, "service.log"));
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
}
