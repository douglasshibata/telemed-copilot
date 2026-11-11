package com.med.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JitsiTokenService {

    @Value("${jitsi.app-id:telemed-copilot}")
    private String appId;

    @Value("${jitsi.secret:telemed_jwt_secret_key_32bytes_long!!}")
    private String secret;

    @Value("${jitsi.domain:meet.jit.si}")
    private String domain;

    @Value("${jitsi.token-validity-seconds:7200}")
    private long validitySeconds;

    public String generateToken(String roomName, String userId, String userName, String email, boolean isDoctor) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + (validitySeconds * 1000));

        Map<String, Object> context = new HashMap<>();
        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("name", userName);
        user.put("email", email);
        context.put("user", user);

        // Security feature: recording claim is restricted strictly to the doctor
        Map<String, Object> features = new HashMap<>();
        features.put("recording", isDoctor);
        features.put("livestreaming", false);
        features.put("screen-sharing", true);
        context.put("features", features);

        return Jwts.builder()
                .issuer(appId)
                .subject(domain)
                .audience().add("jitsi").and()
                .claim("room", roomName)
                .claim("context", context)
                .claim("moderator", isDoctor)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public String getDomain() {
        return domain;
    }
}
