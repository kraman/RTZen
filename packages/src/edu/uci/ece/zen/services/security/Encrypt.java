package edu.uci.ece.zen.services.security;

import java.security.Key;
import java.security.Provider;
import java.security.Security;
import java.util.Iterator;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

public class Encrypt {
    private void printAllProviders() {
        Provider[] providers = Security.getProviders();

        for (int i = 0; i < providers.length; i++) {
            Provider provider = providers[i];

            ZenProperties.logger.log("Provider name: " + provider.getName());
            ZenProperties.logger.log("Provider information: " + provider.getInfo());
            ZenProperties.logger.log("Provider version: " + provider.getVersion());
            Set entries = provider.entrySet();

            Iterator iterator = entries.iterator();

            while (iterator.hasNext()) {
                ZenProperties.logger.log("Property entry: " + iterator.next());
            }
        }
    }

    public static void main(String[] args) {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        try {

            KeyGenerator kg = KeyGenerator.getInstance("DES");
            Key key = kg.generateKey();
            Cipher cipher = Cipher.getInstance("DES");

            byte[] data = "Encrypt Me !".getBytes();
            ZenProperties.logger.log("Original data : " + new String(data));

            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(data);
            ZenProperties.logger.log("Encrypted data: " + new String(result));

            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] original = cipher.doFinal(result);
            ZenProperties.logger.log("Decrypted data: " + new String(original));
        }

        catch (Exception exception) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "main", exception);
        }
    }
}

