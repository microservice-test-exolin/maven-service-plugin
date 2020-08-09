package org.exolin.msp.core;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests für {@link LinuxAbstraction}.
 *
 * @author tomgk
 */
public class LinuxAbstractionTest
{
    private final LinuxAbstraction a = new LinuxAbstraction(new Log(){
        @Override
        public void warn(String string){}
        
        @Override
        public void info(String string){}
    });
    
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
    
    @Test
    public void testSystem2() throws IOException
    {
        boolean win = System.getProperty("os.name").startsWith("Windows");
        
        String[] winCmd = {"cmd", "/C", "\"echo abc\""};
        String[] linCmd = {"/bin/echo", "abc"};
        
        String str = a.system2(win ? winCmd : linCmd);
        assertEquals("abc"+System.getProperty("line.separator"), str);
    }
}
