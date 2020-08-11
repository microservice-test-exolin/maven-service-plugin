package org.exolin.msp.web.ui.servlet.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author tomgk
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubRepository
{
    private String name;
    private String url;

    public String getName()
    {
        return name;
    }

    public String getUrl()
    {
        return url;
    }
}
