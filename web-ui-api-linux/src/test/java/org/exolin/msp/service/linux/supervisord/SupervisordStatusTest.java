package org.exolin.msp.service.linux.supervisord;

import org.exolin.msp.core.StatusType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SupervisordStatus}
 * 
 * @author tomgk
 */
public class SupervisordStatusTest
{
    @Test
    public void testGetStatus()
    {
        String line = "go-e621-tg-bot                   RUNNING   pid 17252, uptime 0:01:13\n";
        
        SupervisordStatus status = new SupervisordStatus(line);
        
        assertEquals(line, status.getInfo());
        assertEquals((Long)17252l, status.getJavaPID());
        assertEquals(StatusType.ACTIVE, status.getStatus());
    }
}
