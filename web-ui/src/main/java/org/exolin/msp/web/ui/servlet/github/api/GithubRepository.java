package org.exolin.msp.web.ui.servlet.github.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author tomgk
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubRepository
{
    private String name;
    private String html_url;

    public String getName()
    {
        return name;
    }

    public String getHtml_url()
    {
        return html_url;
    }
}
