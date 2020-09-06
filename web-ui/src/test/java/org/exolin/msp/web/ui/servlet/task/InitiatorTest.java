package org.exolin.msp.web.ui.servlet.task;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für {@link Initiator}
 *
 * @author tomgk
 */
public class InitiatorTest
{
    @Test
    public void testParse_NoArgs()
    {
        Initiator i = Initiator.parse("test");
        assertEquals("test", i.type);
        assertEquals(Collections.emptyMap(), i.args);
    }
    
    @Test
    public void testParse_OneArgs()
    {
        Initiator i = Initiator.parse("test[a=1]");
        assertEquals("test", i.type);
        assertEquals(Collections.singletonMap("a", "1"), i.args);
    }
    
    @Test
    public void testParse_TwoArgs()
    {
        Initiator i = Initiator.parse("test[a=1,b=2]");
        assertEquals("test", i.type);
        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        assertEquals(map, i.args);
    }
}
