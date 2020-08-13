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
    @Test
    public void testGenerateFilename()
    {
        assertEquals("deploy-2020-01-02-030405.log", LognameGenerator.generateFilename("deploy", LocalDateTime.of(2020, 1, 2, 3, 4, 5)));
    }
    
    @Test
    public void testGetLogFileTitle_Service()
    {
        assertEquals("Service Log", LognameGenerator.getLogFileTitle("service.log"));
    }
    
    @Test
    public void testGetLogFileTitle_GenericGroup()
    {
        assertEquals("x at 2020-01-02 03:04:05", LognameGenerator.getLogFileTitle("x-2020-01-02-030405.log"));
    }
    
    @Test
    public void testGetLogFileTitle_Task()
    {
        assertEquals("Task x at 2020-01-02 03:04:05", LognameGenerator.getLogFileTitle("task-x-2020-01-02-030405.log"));
    }
    
    @Test
    public void testGetLogFileTitle_Other()
    {
        assertEquals("abc.log", LognameGenerator.getLogFileTitle("abc.log"));
    }
}
