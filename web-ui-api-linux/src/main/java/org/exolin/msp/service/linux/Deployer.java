package org.exolin.msp.service.linux;

import java.nio.file.Path;

/**
 *
 * @author tomgk
 */
public interface Deployer
{
    String[] deployMaven();
    String[] buildAndDeployGo(Path gitRoot, String serviceName);
    
    static Deployer getDeployer()
    {
        return new LinuxDeployer();
    }
}
