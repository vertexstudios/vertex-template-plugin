package org.vertex.gradle;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.crypto.Cipher;
import java.io.*;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class EncryptJarTask {

    public static void main(String[] args) {
        File buildOutputFolder = new File(".", "build/libs");

        for (File jarFile : buildOutputFolder.listFiles()) {
            Map<String, byte[]> jarFiles = new HashMap<>();

            if (jarFile.getName().contains("client")) {
                Manifest manifest = null;

                try {
                    byte[] publicKey = EncryptJarTask.class.getClassLoader().getResourceAsStream("rsa_public_key").readAllBytes();
                    byte[] privateKey = EncryptJarTask.class.getClassLoader().getResourceAsStream("rsa_private_key").readAllBytes();

                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    EncodedKeySpec privateKeySpec = new X509EncodedKeySpec(privateKey);
                    PrivateKey key = keyFactory.generatePrivate(privateKeySpec);


                    try (JarInputStream zis = new JarInputStream(new FileInputStream(jarFile))) {
                        JarEntry zipEntry;
                        while ((zipEntry = zis.getNextJarEntry()) != null) {
                            jarFiles.put(zipEntry.getName(), IOUtils.toByteArray(zis));
                        }

                        manifest = zis.getManifest();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    for (Map.Entry<String, byte[]> entry : jarFiles.entrySet()) {
                        try {
                            Cipher encryptCipher = Cipher.getInstance("RSA");
                            encryptCipher.init(Cipher.ENCRYPT_MODE, key);
                            entry.setValue(encryptCipher.doFinal(entry.getValue()));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }


                try (JarOutputStream zos = new JarOutputStream(new FileOutputStream(new File(buildOutputFolder, FilenameUtils.removeExtension(jarFile.getName()) + ".vx")), manifest)) {
                    for (Map.Entry<String, byte[]> entry : jarFiles.entrySet()) {
                        zos.putNextEntry(new JarEntry(entry.getKey()));
                        zos.write(entry.getValue());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                if (!jarFile.delete()) {
                    throw new RuntimeException("Tried to delete the input jar but had no success");
                }
            }
        }
    }

}
