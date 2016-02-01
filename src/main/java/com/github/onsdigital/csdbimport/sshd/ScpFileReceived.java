package com.github.onsdigital.csdbimport.sshd;

import java.nio.file.Path;

/**
 * Created by iankent on 29/01/2016.
 */
public interface ScpFileReceived {
    void onScpFileReceived(Path path);
}
