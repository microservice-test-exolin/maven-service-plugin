package org.exolin.msp.web.ui.servlet.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 *
 * @author tomgk
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubCommit
{
    private String id;
    private String message;
    private String timestamp;
    private String url;
    
    private List<String> added;
    private List<String> removed;
    private List<String> modified;

    public String getId()
    {
        return id;
    }
}
