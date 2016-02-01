package com.github.onsdigital.csdbimport;

import com.github.davidcarboni.restolino.framework.Startup;
import com.github.onsdigital.csdbimport.helpers.Configuration;
import com.github.onsdigital.csdbimport.sshd.AuthorizedKeysDecoder;
import com.github.onsdigital.csdbimport.sshd.PublicKeyAuthenticator;
import com.github.onsdigital.csdbimport.sshd.SSHServer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
        PublicKeyAuthenticator publicKeyAuthenticator = new AuthorizedKeysDecoder(Configuration.SCP_AUTHORIZED_KEYS);

        SSHServer sshServer = new SSHServer();
        sshServer.setPublicKeyAuthenticator(publicKeyAuthenticator);
        sshServer.setScpRootDir(Configuration.SCP_ROOT_DIR);

        sshServer.setScpFileReceivedHandler((Path path) -> {
            try {
                byte bytes[] = Files.readAllBytes(FileSystems.getDefault().getPath(Configuration.SCP_ROOT_DIR + path.toString()));
                ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes));
                ZipEntry entry;
                while((entry = zis.getNextEntry())!=null) {
                    System.out.println(entry.getName());
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
