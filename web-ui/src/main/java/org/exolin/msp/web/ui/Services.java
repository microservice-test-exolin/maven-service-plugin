package org.exolin.msp.web.ui;

import java.util.List;

/**
 *
 * @author tomgk
 */
public interface Services
{
    List<Service> getServices();

    public Service getServices(String serviceName);
}
