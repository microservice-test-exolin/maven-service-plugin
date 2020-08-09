package org.exolin.msp.web.ui.stub;

import java.util.List;
import org.exolin.msp.web.ui.Service;
import org.exolin.msp.web.ui.Services;

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
}
