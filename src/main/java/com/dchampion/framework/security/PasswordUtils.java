package com.dchampion.framework.security;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Password utilities.
 */
@Component
public class PasswordUtils {

    @Value("${framework.breach-api-uri-root:https://api.pwnedpasswords.com/range/}")
    private String breachApiUriRoot;

    @Value("${framework.breach-api-hash-algo:SHA-1}")
    private String breachApiHashAlgo;

    private static final Logger log = LoggerFactory.getLogger(PasswordUtils.class);

    /**
     * Returns {@code true} if the supplied password has been previously leaked in a
     * known data breach; otherwise {@code false}.
     *
     * @param password The password to test for a leak.
     *
     * @return {@code true} if the password has been leaked; {@code false} otherwise.
     */
    public boolean isLeaked(String password) {

        boolean isBreached = false;
        try {
            MessageDigest md = MessageDigest.getInstance(breachApiHashAlgo);
            char[] chars = Hex.encode(md.digest(password.getBytes()));

            String prefix = new String(chars, 0, 5).toUpperCase();
            String url = breachApiUriRoot + prefix;

            RestTemplate client = new RestTemplate();
            RequestEntity<Void> request =
                RequestEntity.get(new URI(url)).accept(
                    MediaType.TEXT_HTML).header("Add-Padding", "true").build();

            ResponseEntity<String> response = client.exchange(request, String.class);
            String[] body = response.getBody().split("\n");

            String suffix = new String(chars, 5, chars.length-5).toUpperCase();
            for (int i=0; i<body.length && !isBreached; i++) {
                if (body[i].startsWith(suffix) && !body[i].endsWith(":0")) {
                    isBreached = true;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            log.warn("Unsupported hash algorigthm: " + breachApiHashAlgo);
        } catch (URISyntaxException e) {
            log.warn(e.getMessage());
        }
        return isBreached;
    }
}
