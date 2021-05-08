package org.exolin.msp.service.linux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.exolin.msp.service.GitRepository;

/**
 *
 * @author tomgk
 */
public abstract class AbstractGitRepository implements GitRepository
{
    public static String getRepositoryUrl(Path gitRepository) throws IOException
    {
        /*
        [remote "origin"]
        url = $URL
        */
        
        List<String> lines = Files.readAllLines(gitRepository.resolve(".git/config"));
        
        String URL = "url = ";
        
        int sectionLine = lines.indexOf("[remote \"origin\"]");
        if(sectionLine == -1)
            throw new IOException("No section remote origin");
        
        int nextSectionLine = findFirstStartingWith(lines, "[", sectionLine+1, lines.size());
        if(nextSectionLine == -1) nextSectionLine = lines.size();
        
        int urlLine = findFirstStartingWith(lines, URL, sectionLine+1, lines.size());
        if(urlLine == -1 || urlLine > nextSectionLine)  //nicht in (richtiger) section gefundne
            throw new IOException("no remote origin url\n"+
                    "sectionLine:"+sectionLine+"\n"+
                    "URlLine:"+urlLine+"\n"+
                    "nextSectionLine:"+nextSectionLine+"\n"+
                    String.join("\n", lines));
        
        String repo = lines.get(urlLine).trim().substring(URL.length());
        if(repo.endsWith(".git"))
            repo = repo.substring(0, repo.length()-4);
        
        return repo;
    }
    
    private static int findFirstStartingWith(List<String> list, String startString, int start, int end)
    {
        for(int i=start;i<end;++i)
            if(list.get(i).trim().startsWith(startString))
                return i;
        
        return -1;
    }
    
    static Path getGitRoot(Path path)
    {
        for(Path p=path;p!=null;p=p.getParent())
        {
            if(Files.exists(p.resolve(".git")))
                return p;
        }
        
        throw new IllegalArgumentException("Not a git repository: "+path);
    }
    
    @Override
    public final String getRepositoryUrl() throws IOException
    {
        return getRepositoryUrl(getLocalGitRoot());
    }
}
