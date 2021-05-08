package org.exolin.msp.service.linux;

import java.nio.file.Path;

/**
 *
 * @author tomgk
 */
public class LinuxDeployer implements Deployer
{
    @Override
    public String[] deployMaven()
    {
        return new String[]{"/bin/bash", "-c", "/root/repos/deploy.sh"};
    }

    @Override
    public String[] buildAndDeployGo(Path gitRoot, String serviceName)
    {
        return new String[]{"/bin/bash", "-c", "/root/repos/go-deploy/setup.sh -s "+gitRoot+" -o /root/apps/"+serviceName+"/"+serviceName+" -a "+serviceName+" -f"};
    }
}
