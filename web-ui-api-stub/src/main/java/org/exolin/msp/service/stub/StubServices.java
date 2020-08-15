package org.exolin.msp.service.stub;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.exolin.msp.service.Service;
import org.exolin.msp.service.Services;

/**
 *
 * @author tomgk
 */
public class StubServices implements Services
{
    private final List<Service> services;

    public StubServices(List<Service> services)
    {
        this.services = services;
    }

    @Override
    public List<Service> getServices()
    {
        return services;
    }

    @Override
    public Service getService(String serviceName)
    {
        return services.stream()
                .filter(s -> s.getName().equals(serviceName))
                .findFirst().orElse(null);
    }

    @Override
    public List<Service> getServicesFromRepositoryUrl(String url) throws IOException
    {
        return Collections.emptyList();
    }
}
