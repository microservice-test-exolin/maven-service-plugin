package org.exolin.msp.service;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tomgk
 */
public class LinuxAbstractionTest
{
    private final LinuxAbstraction a = new LinuxAbstraction();
    
    @Test
    public void testGetStatus_Okay() throws Exception
    {
        String okay = "â mittens-discord.service - Mittens Discord\n" +
"   Loaded: loaded (/etc/systemd/system/mittens-discord.service; disabled)\n" +
"   Active: active (running) since Thu 2020-07-30 20:17:05 UTC; 3 days ago\n" +
" Main PID: 3933 (bash)\n" +
"   CGroup: /system.slice/mittens-discord.service\n" +
"           ââ3933 /bin/bash /home/exolin/services/mittens-discord/bin/start.sh\n" +
"           ââ3934 /usr/bin/java -jar /home/exolin/services/mittens-discord/bin/mittens-discord.jar";
        
        assertTrue(a.parse(okay));
    }
    
    @Test
    public void testGetStatus_Failed() throws Exception
    {
        String okay = "â empty-service.service\n" +
"   Loaded: not-found (Reason: No such file or directory)\n" +
"   Active: failed (Result: exit-code) since Thu 2020-07-30 16:48:46 UTC; 3 days ago\n" +
" Main PID: 20881 (code=exited, status=143)\n" +
"\n" +
"Jul 30 15:58:19 exolin.org java[20881]: Started\n" +
"Jul 30 16:48:46 exolin.org java[20881]: Exiting";
        
        assertFalse(a.parse(okay));
    }
    
    @Test
    public void testGetStatus_Invalid() throws Exception
    {
        try{
            a.parse("xyz");
            fail();
        }catch(UnsupportedOperationException e){
            assertEquals("Can't parse:\nxyz", e.getMessage());
        }
    }
}
