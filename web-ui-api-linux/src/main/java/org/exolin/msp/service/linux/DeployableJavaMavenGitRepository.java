package org.exolin.msp.service.linux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.exolin.msp.service.GitRepository;

/**
 * A git repository that contains a maven java project that can be deployed using /root/repos/deploy.sh.
 * The build happens with the root project in the repository,
 * the deploy happens with a specific project in the repository which can be
 * the root project or a (sub)module of the root.
 *
 * @author tomgk
 */
public class DeployableJavaMavenGitRepository implements GitRepository
{
    private final LinuxService linuxService;
    private final Path localServiceMavenProject;

    public DeployableJavaMavenGitRepository(LinuxService linuxService, Path localServiceMavenProject)
    {
        this.linuxService = linuxService;
        this.localServiceMavenProject = localServiceMavenProject;
    }

    @Override
    public boolean isBuildOrDeployProcessRunning()
    {
        return linuxService.isBuildOrDeployProcessRunning();
    }
    
    @Override
    public boolean supportsBuildAndDeployment() throws IOException
    {
        return true;
        /*try{
            getGitRepository().getLocalServiceMavenProject();
            return true;
        }catch(UnsupportedOperationException e){
            LOGGER.info("Couldn't determine original path", e);
            return false;
        }*/
    }

    @Override
    public String getRepositoryUrl() throws IOException
    {
        return getRepositoryUrl(getLocalGitRoot());
    }
    
    @Override
    public Path getLocalGitRoot() throws IOException
    {
        return getGitRoot(getLocalServiceMavenProject());
    }
    
    @Override
    public Path getLocalServiceMavenProject() throws IOException
    {
        return localServiceMavenProject;
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
    
    
    @Override
    public void build(boolean asynch, String initiator) throws IOException, InterruptedException
    {
        Path gitRoot = getLocalGitRoot();
        
        String[] cmd = {"/bin/bash", "-c", "git pull && mvn package"};
        
        linuxService.start(gitRoot, LinuxService.TASK_BUILD, cmd, asynch, initiator);
    }
    
    @Override
    public void deploy(boolean asynch, String initiator) throws IOException, InterruptedException
    {
        Path serviceSrcDirectory = getLocalServiceMavenProject();
        
        String[] cmd = {"/bin/bash", "-c", "/root/repos/deploy.sh"};
        
        linuxService.start(serviceSrcDirectory, LinuxService.TASK_DEPLOY, cmd, asynch, initiator);
    }
}
