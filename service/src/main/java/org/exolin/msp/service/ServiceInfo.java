package org.exolin.msp.service;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author tomgk
 */
public class ServiceInfo
{
    private final String serviceName;
    private final String serviceTitle;
    private final String serviceUser;

    public ServiceInfo(String serviceName, String serviceTitle, String serviceUser)
    {
        this.serviceName = serviceName;
        this.serviceTitle = serviceTitle;
        this.serviceUser = serviceUser;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public String getServiceTitle()
    {
        return serviceTitle;
    }

    public String getServiceUser()
    {
        return serviceUser;
    }
    
    public static Path getBaseDirectory(String serviceUser, String serviceName)
    {
        return Paths.get("/home/"+serviceUser+"/services/"+serviceName+"/");
    }
    
    public static final String START_SH = "start.sh";
}
