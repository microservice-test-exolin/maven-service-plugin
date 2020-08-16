package org.exolin.msp.web.ui.servlet;

import java.util.AbstractList;
import java.util.List;

/**
 *
 * @author tomgk
 */
public class ReverseList<T> extends AbstractList<T>
{
    private final List<T> list;

    public ReverseList(List<T> list)
    {
        this.list = list;
    }

    @Override
    public T get(int index)
    {
        return list.get(list.size()-1-index);
    }

    @Override
    public int size()
    {
        return list.size();
    }
}
