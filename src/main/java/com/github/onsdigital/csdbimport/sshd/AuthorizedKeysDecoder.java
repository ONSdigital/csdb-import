package com.github.onsdigital.csdbimport.sshd;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Scanner;

// https://stackoverflow.com/questions/3531506/using-public-key-from-authorized-keys-with-java-security

public class AuthorizedKeysDecoder implements PublicKeyAuthenticator {
    private byte[] bytes;
    private int pos;

    private ArrayList<PublicKey> publicKeys = new ArrayList<>();

    public AuthorizedKeysDecoder() {
        this(System.getProperty("user.home") + "/.ssh/authorized_keys");
    }

    @Override
    public boolean isValid(String user, PublicKey key) {
        for (PublicKey k:publicKeys) {
            if(k.equals(key)) return true;
        }
        return false;
    }

    public AuthorizedKeysDecoder(String authorizedKeys) {
        try {
            File file = new File(authorizedKeys);
            Scanner scanner = new Scanner(file).useDelimiter("\n");
            while (scanner.hasNext()) {
                PublicKey key = this.decodePublicKey(scanner.next());
                publicKeys.add(key);
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PublicKey decodePublicKey(String keyLine) throws Exception {
        bytes = null;
        pos = 0;

        // look for the Base64 encoded part of the line to decode
        // both ssh-rsa and ssh-dss begin with "AAAA" due to the length bytes
        for (String part : keyLine.split(" ")) {
            if (part.startsWith("AAAA")) {
                bytes = Base64.decodeBase64(part);
                break;
            }
        }
        if (bytes == null) {
            throw new IllegalArgumentException("no Base64 part to decode");
        }

        String type = decodeType();
        if (type.equals("ssh-rsa")) {
            BigInteger e = decodeBigInt();
            BigInteger m = decodeBigInt();
            RSAPublicKeySpec spec = new RSAPublicKeySpec(m, e);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } else if (type.equals("ssh-dss")) {
            BigInteger p = decodeBigInt();
            BigInteger q = decodeBigInt();
            BigInteger g = decodeBigInt();
            BigInteger y = decodeBigInt();
            DSAPublicKeySpec spec = new DSAPublicKeySpec(y, p, q, g);
            return KeyFactory.getInstance("DSA").generatePublic(spec);
        } else {
            throw new IllegalArgumentException("unknown type " + type);
        }
    }

    private String decodeType() {
        int len = decodeInt();
        String type = new String(bytes, pos, len);
        pos += len;
        return type;
    }

    private int decodeInt() {
        return ((bytes[pos++] & 0xFF) << 24) | ((bytes[pos++] & 0xFF) << 16)
                | ((bytes[pos++] & 0xFF) << 8) | (bytes[pos++] & 0xFF);
    }

    private BigInteger decodeBigInt() {
        int len = decodeInt();
        byte[] bigIntBytes = new byte[len];
        System.arraycopy(bytes, pos, bigIntBytes, 0, len);
        pos += len;
        return new BigInteger(bigIntBytes);
    }
}
