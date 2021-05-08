package org.exolin.msp.service.linux;

import org.exolin.msp.core.StatusType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author tomgk
 */
public class SupervisordApplicationInstanceTest
{
    @Test
    public void testGetStatus()
    {
        String line = "go-e621-tg-bot                   RUNNING   pid 17252, uptime 0:01:13\n";
        
        SupervisordApplicationInstance.SupervisordStatus status = new SupervisordApplicationInstance.SupervisordStatus(line);
        
        assertEquals(line, status.getInfo());
        assertEquals((Long)17252l, status.getJavaPID());
        assertEquals(StatusType.ACTIVE, status.getStatus());
    }
}
