package org.exolin.msp.service.pm;

/**
 *
 * @author tomgk
 */
public class BuildOrDeployAlreadyRunningException extends RuntimeException
{
    public BuildOrDeployAlreadyRunningException(String message)
    {
        super(message);
    }
}
