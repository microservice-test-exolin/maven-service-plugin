package org.exolin.msp.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.util.Set;

/**
 *
 * @author tomgk
 */
public class Generator
{
    public static void createServiceFile(File serviceFile, String serviceTitle, String serviceUser, Path startSh) throws IOException
    {
        PrintWriter w = new PrintWriter(serviceFile);

        w.println("[Unit]");
        w.println("Description="+serviceTitle);
        w.println("After=network.target");
        w.println("StartLimitIntervalSec=0");
        w.println();
        w.println("[Service]");
        w.println("Type=simple");
        w.println("Restart=always");
        w.println("RestartSec=1");
        w.println("User="+serviceUser);
        w.println("ExecStart=/bin/bash "+startSh.toFile().toString().replace("\\", "/"));  //TODO: replace \ with / is uhh given that this is ultimatily not used on windows
        w.println();
        w.println("[Install]");
        w.println("WantedBy=multi-user.target");
        w.close();
        if(w.checkError())
            throw new IOException("Failed to write file "+serviceFile);
    }
    
    private static void writeSysProp(Writer w, String name, String value) throws IOException
    {
        //safety check
        if(name.contains(" ") || value.contains(" "))
            throw new IllegalArgumentException();
        
        w.append(" -D").append(name).append("=").append(value);
    }
    
    public static void createStartSh(File file, String serviceName, String serviceUser, String jarName, boolean useConfigDirectory) throws IOException
    {
        Path path = file.toPath();
        
        try(BufferedWriter w = Files.newBufferedWriter(file.toPath()))
        {
            w.write("#Generated by "+Constants.ID+" at "+LocalDateTime.now());
            w.newLine();
            
            w.write("set -e");
            w.newLine();

            w.write("NAME="+serviceName);
            w.newLine();

            w.write("DIR=/home/"+serviceUser+"/services/$NAME");
            w.newLine();

            w.write("cd $DIR/bin");
            w.newLine();

            w.write("/usr/bin/java");
            writeSysProp(w, "system.baseDirectory", "$DIR");
            writeSysProp(w, "system.logDirectory", "$DIR/log");
            
            if(useConfigDirectory)
                writeSysProp(w, "system.configDirectory", "$DIR/cfg");
            
            w.write(" -jar ");
           /**/w.append("$DIR/bin/").append(jarName);
            w.write(" >> $DIR/log/$NAME.log ");
            w.write("2>> $DIR/log/$NAME.error.log");
            w.newLine();

            w.write("echo Started $NAME");
            w.newLine();
        }
        
        if(path.getFileSystem().supportedFileAttributeViews().contains("posix"))
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-r--r--"));
    }
}
