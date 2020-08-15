package org.exolin.msp.service;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author tomgk
 */
public interface Services
{
    List<Service> getServices() throws IOException;

    public Service getService(String serviceName) throws IOException;
    
    public List<Service> getServicesFromRepositoryUrl(String url) throws IOException;
}
