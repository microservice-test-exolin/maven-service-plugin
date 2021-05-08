package org.exolin.msp.service.linux;

import java.io.IOException;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests für {@link DeployableJavaMavenGitRepository}
 * 
 * @author tomgk
 */
public class DeployableJavaMavenGitRepositoryTest
{
    @Test
    public void testGetRepositoryUrl() throws IOException
    {
        Assertions.assertEquals("https://github.com/microservice-test-exolin/maven-service-plugin", DeployableJavaMavenGitRepository.getRepositoryUrl(Paths.get("..").toAbsolutePath().normalize()));
    }
}
