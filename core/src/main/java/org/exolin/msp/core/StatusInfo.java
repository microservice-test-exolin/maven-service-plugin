package org.exolin.msp.core;

/**
 *
 * @author tomgk
 */
public interface StatusInfo
{
    StatusType getStatus();
    String getInfo();
    UnknowableBoolean isStartAtBootEnabled();
    public String getMemory();
    
    enum UnknowableBoolean
    {
        FALSE,
        TRUE,
        UNKNOWN;

        static UnknowableBoolean of(boolean b)
        {
            return b ? TRUE : FALSE;
        }

        static UnknowableBoolean ofNullable(Boolean b)
        {
            return b != null ? of(b) : UNKNOWN;
        }

        @Override
        public String toString()
        {
            return name().toLowerCase();
        }
    }
}
