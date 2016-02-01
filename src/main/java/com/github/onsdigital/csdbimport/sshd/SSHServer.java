package com.github.onsdigital.csdbimport.sshd;

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.scp.ScpTransferEventListener;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;

public class SSHServer {
    protected PublicKeyAuthenticator publicKeyAuthenticator;
    protected String scpRootDir;
    protected ScpFileReceived onScpFileReceivedHandler;

    protected SshServer sshd;
    protected FileSystem vfs;

    public SSHServer() {}

    public void setPublicKeyAuthenticator(PublicKeyAuthenticator publicKeyAuthenticator) {
        this.publicKeyAuthenticator = publicKeyAuthenticator;
    }

    public void setScpFileReceivedHandler(ScpFileReceived onScpFileReceived) {
        this.onScpFileReceivedHandler = onScpFileReceived;
    }

    public void setScpRootDir(String scpRootDir) {
        this.scpRootDir = scpRootDir;
    }

    public void start() throws IOException {
        final VirtualFileSystemFactory virtualFileSystemFactory = new VirtualFileSystemFactory(scpRootDir);

        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(2323);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshd.setFileSystemFactory((Session session) -> {
            vfs = virtualFileSystemFactory.createFileSystem(session);
            return vfs;
        });

        sshd.setPublickeyAuthenticator((String s, PublicKey publicKey, ServerSession serverSession) ->
            publicKeyAuthenticator != null && publicKey instanceof RSAPublicKey && publicKeyAuthenticator.isValid(s, publicKey)
        );

        ScpCommandFactory scp = new ScpCommandFactory();
        scp.addEventListener(new ScpTransferEventListener() {
            @Override
            public void startFileEvent(FileOperation fileOperation, Path path, long l, Set<PosixFilePermission> set) {
                System.out.println("startFileEvent (" + (fileOperation == FileOperation.SEND ? "SEND" : "RECEIVE") + ") " + path);
            }

            @Override
            public void endFileEvent(FileOperation fileOperation, Path path, long l, Set<PosixFilePermission> set, Throwable throwable) {
                System.out.println("endFileEvent (" + (fileOperation == FileOperation.SEND ? "SEND" : "RECEIVE") + ") " + path);

                if(throwable != null) {
                    throwable.printStackTrace();
                    return;
                }

                if(onScpFileReceivedHandler != null) {
                    onScpFileReceivedHandler.onScpFileReceived(path);
                }
            }

            @Override
            public void startFolderEvent(FileOperation fileOperation, Path path, Set<PosixFilePermission> set) {
                System.out.println("startFolderEvent (" + (fileOperation == FileOperation.SEND ? "SEND" : "RECEIVE") + ") " + path);
            }

            @Override
            public void endFolderEvent(FileOperation fileOperation, Path path, Set<PosixFilePermission> set, Throwable throwable) {
                System.out.println("endFolderEvent (" + (fileOperation == FileOperation.SEND ? "SEND" : "RECEIVE") + ") " + path);
                if(throwable != null) {
                    throwable.printStackTrace();
                }
            }
        });

        sshd.setCommandFactory(scp);
        sshd.start();
    }
}
