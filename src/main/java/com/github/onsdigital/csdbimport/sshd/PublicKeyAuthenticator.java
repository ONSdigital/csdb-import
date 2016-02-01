package com.github.onsdigital.csdbimport.sshd;

import java.security.PublicKey;

/**
 * Created by iankent on 29/01/2016.
 */
public interface PublicKeyAuthenticator {
    boolean isValid(String user, PublicKey key);
}
