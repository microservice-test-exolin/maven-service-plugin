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
}
