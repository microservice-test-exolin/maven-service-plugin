package org.exolin.msp.service.pm;

/**
 *
 * @author tomgk
 */
public class TaskAlreadyRunningException extends RuntimeException
{
    public TaskAlreadyRunningException(String message)
    {
        super(message);
    }
}
