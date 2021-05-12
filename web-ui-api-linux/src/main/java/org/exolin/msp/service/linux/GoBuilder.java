package org.exolin.msp.service.linux;

import java.nio.file.Path;
import org.exolin.msp.service.pm.ProcessManager;

/**
 *
 * @author tomgk
 */
public class GoBuilder
{
    private static final String GO = "/usr/local/go/bin/go";
    
    public static String[] createBuildCmd(ProcessManager manager, Path outputDirectory)
    {
        //String cmd = GO+" get -v -t -d ./... && GO+" build -ldflags "-X main.gitCommit=$GIT_COMMIT" -o $OUTPUT || failed Build
        String cmd = GO+" get -v -t -d ./... && "+GO+" build -o "+outputDirectory.toFile().getAbsolutePath();
        
        return LinuxAbstraction.createBashExecutionCmd(cmd);
    }
}
