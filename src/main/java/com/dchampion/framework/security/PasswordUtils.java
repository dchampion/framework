package com.dchampion.framework.security;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.DrbgParameters;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.DrbgParameters.Capability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Value("${framework.hash-work-factor:12}")
    private String pwHashWorkFactor;
    private PasswordEncoder encoder;

    private static final Logger log = LoggerFactory.getLogger(PasswordUtils.class);

    /**
     * Returns {@code true} if the supplied password has been previously leaked in a
     * known data breach; otherwise {@code false}.
     *
     * @param password The password to test for a leak.
     *
     * @return {@code true} if the password has been leaked; {@code false}
     *         otherwise.
     */
    public boolean isLeaked(String password) {

        boolean leaked = false;
        try {
            MessageDigest md = MessageDigest.getInstance(breachApiHashAlgo);
            char[] chars = Hex.encode(md.digest(password.getBytes(StandardCharsets.UTF_8)));

            String prefix = new String(chars, 0, 5).toUpperCase();
            String url = breachApiUriRoot + prefix;

            RestTemplate client = new RestTemplate();
            RequestEntity<Void> request = RequestEntity.get(new URI(url)).accept(MediaType.TEXT_HTML)
                    .header("Add-Padding", "true").build();

            ResponseEntity<String> response = client.exchange(request, String.class);
            String body = response.getBody();
            if (body != null) {
                String[] strings = body.split("\n");

                String suffix = new String(chars, 5, chars.length - 5).toUpperCase();
                for (int i = 0; i < strings.length && !leaked; i++) {
                    if (strings[i].startsWith(suffix) && !strings[i].endsWith(":0")) {
                        leaked = true;
                    }
                }
            }
        } catch (NoSuchAlgorithmException e) {
            log.warn("Unsupported hash algorigthm: " + breachApiHashAlgo);
        } catch (URISyntaxException e) {
            log.warn(e.getMessage());
        }
        return leaked;
    }

    /**
     * Returns a cryptographically strong password encoder.
     *
     * @return A cryptographically strong password encoder.
     */
    public PasswordEncoder getEncoder() {
        if (encoder == null) {
            SecureRandom secureRandom;
            try {
                // Requires Java 9+
                secureRandom = SecureRandom.getInstance("DRBG", DrbgParameters.instantiation(
                    256, Capability.PR_AND_RESEED, String.valueOf(System.nanoTime()).getBytes()));
            } catch (NoSuchAlgorithmException nsae) {
                // This shouldn't happen, but if it does print a warning.
                secureRandom = new SecureRandom();
                log.warn("SecureRandom.getInstance(\"DRBG\") threw a "
                    + "NoSuchAlgorithmException; defaulting to " + secureRandom.getAlgorithm());
            }
            log.info("Using SecureRandom algorigthm: " + secureRandom.getAlgorithm());
            log.info("Using SecureRandom parameters: " + secureRandom.getParameters().toString());
            encoder = new BCryptPasswordEncoder(Integer.parseInt(pwHashWorkFactor), secureRandom);
        }
        return encoder;
    }
}
