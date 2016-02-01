package com.github.onsdigital.csdbimport;

import com.github.davidcarboni.restolino.framework.Startup;
import com.github.onsdigital.csdbimport.helpers.Configuration;
import com.github.onsdigital.csdbimport.sshd.AuthorizedKeysDecoder;
import com.github.onsdigital.csdbimport.sshd.PublicKeyAuthenticator;
import com.github.onsdigital.csdbimport.sshd.SSHServer;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by iankent on 29/01/2016.
 */
public class CSDBImport implements Startup {
    @Override
    public void init() {
        PublicKeyAuthenticator publicKeyAuthenticator = new AuthorizedKeysDecoder(Configuration.SCP.getAuthorizedKeys());

        SSHServer sshServer = new SSHServer();
        sshServer.setPublicKeyAuthenticator(publicKeyAuthenticator);
        sshServer.setScpRootDir(Configuration.SCP.getRootDir());

        String p = Configuration.CSDB.getCsdbDataDir();
        System.out.println("CSDB Path: " + p);
        File csdbFile = new File(p);
        if(!csdbFile.exists() && !csdbFile.mkdirs()) {
            throw new RuntimeException("csdb path not found");
        }
        Path csdbPath = FileSystems.getDefault().getPath(csdbFile.getPath());

        sshServer.setScpFileReceivedHandler((Path path) -> {
            System.out.println("Received zip file: " + path.toString());
            try {
                byte bytes[] = Files.readAllBytes(FileSystems.getDefault().getPath(Configuration.SCP.getRootDir() + path.toString()));
                ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes));
                ZipEntry entry;
                while((entry = zis.getNextEntry())!=null) {
                    System.out.println("Received CSDB file: " + entry.getName());
                    if(!entry.isDirectory()) {
                        Path dest = csdbPath.resolve(entry.getName());
                        Files.copy(zis, dest);
                        System.out.println("CSDB file written to: " + dest.toString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        try {
            sshServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
