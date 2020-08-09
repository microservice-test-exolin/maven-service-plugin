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
    public void testSystem2() throws IOException
    {
        boolean win = System.getProperty("os.name").startsWith("Windows");
        
        String[] winCmd = {"cmd", "/C", "\"echo abc\""};
        String[] linCmd = {"/bin/echo", "abc"};
        
        String str = a.system2(win ? winCmd : linCmd);
        assertEquals("abc"+System.getProperty("line.separator"), str);
    }
}
