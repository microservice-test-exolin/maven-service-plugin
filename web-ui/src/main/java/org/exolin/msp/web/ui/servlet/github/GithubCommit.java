package org.exolin.msp.web.ui.servlet.github;

import java.util.List;

/**
 *
 * @author tomgk
 */
public class GithubCommit
{
    private String id;
    private String message;
    private String timestamp;
    private String url;
    
    private List<String> added;
    private List<String> removed;
    private List<String> modified;
}
