package org.exolin.msp.core;

/**
 *
 * @author tomgk
 */
public interface StatusInfo
{
    StatusType getStatus();
    String getInfo();
    boolean isStartAtBootEnabled();
}
