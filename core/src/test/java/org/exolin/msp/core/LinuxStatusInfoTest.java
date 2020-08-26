package org.exolin.msp.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author tomgk
 */
public class LinuxStatusInfoTest
{
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
        
        assertEquals(StatusType.ACTIVE, LinuxStatusInfo.parse(okay));
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
        
        assertEquals(StatusType.FAILED, LinuxStatusInfo.parse(okay));
    }
    
    @Test
    public void testGetStatus_Inactive() throws Exception
    {
        String okay = "â empty-service.service\n" +
"   Loaded: not-found (Reason: No such file or directory)\n" +
"   Active: inactive (Result: exit-code) since Thu 2020-07-30 16:48:46 UTC; 3 days ago\n" +
" Main PID: 20881 (code=exited, status=143)\n" +
"\n" +
"Jul 30 15:58:19 exolin.org java[20881]: Started\n" +
"Jul 30 16:48:46 exolin.org java[20881]: Exiting";
        
        assertEquals(StatusType.INACTIVE, LinuxStatusInfo.parse(okay));
    }
    
    @Test
    public void testGetStatus_Invalid() throws Exception
    {
        assertEquals(StatusType.UNKNOWN, LinuxStatusInfo.parse("xyz"));
    }
    
    @Test
    public void testIsEnabled()
    {
        String txt = "â service-web-ui.service - Web-UI for managing services\n" +
"   Loaded: loaded (/etc/systemd/system/service-web-ui.service; enabled; vendor preset: enabled)\n" +
"   Active: active (running) since Wed 2020-08-26 21:34:16 CEST; 5min ago\n" +
" Main PID: 3491 (bash)\n" +
"   CGroup: /system.slice/service-web-ui.service\n" +
"           ââ3491 /bin/bash /home/root/services/service-web-ui/start.sh\n" +
"           ââ3492 /usr/bin/java -Dsystem.baseDirectory=/home/root/services/service-web-ui -Dsystem.logDirectory=/home/root/services/service-web-ui/log -jar /home/root/services/service-web-ui/bin/service-web-ui-1.0-SNAPSHOT.jar";
        
        assertTrue(new LinuxStatusInfo(txt).isStartAtBootEnabled());
    }
    
    @Test
    public void testIsDisabled()
    {
        String txt = "â milkboi-telegram.service - Milkboi Telegram\n" +
"   Loaded: loaded (/etc/systemd/system/milkboi-telegram.service; disabled; vendor preset: enabled)\n" +
"   Active: active (running) since Wed 2020-08-26 21:36:18 CEST; 3min 54s ago\n" +
" Main PID: 3535 (bash)\n" +
"   CGroup: /system.slice/milkboi-telegram.service\n" +
"           ââ3535 /bin/bash /home/exolin/services/milkboi-telegram/start.sh\n" +
"           ââ3536 /usr/bin/java -Dsystem.baseDirectory=/home/exolin/services/milkboi-telegram -Dsystem.logDirectory=/home/exolin/services/milkboi-telegram/log -jar /home/exolin/services/milkboi-telegram/bin/milkboi-telegram-1.0-SNAPSHOT.";
        
        assertFalse(new LinuxStatusInfo(txt).isStartAtBootEnabled());
    }
}
