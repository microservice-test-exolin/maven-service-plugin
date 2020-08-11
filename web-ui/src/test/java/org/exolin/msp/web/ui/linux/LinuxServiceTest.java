package org.exolin.msp.web.ui.linux;

import java.io.IOException;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests für {@link LinuxService}
 * 
 * @author tomgk
 */
public class LinuxServiceTest
{
    @Test
    public void testGetRepositoryUrl() throws IOException
    {
        Assertions.assertEquals("https://github.com/microservice-test-exolin/maven-service-plugin", LinuxService.getRepositoryUrl(Paths.get("..").toAbsolutePath().normalize()));
    }
}
