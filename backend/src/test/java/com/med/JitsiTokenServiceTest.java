package com.med;

import com.med.service.JitsiTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JitsiTokenServiceTest {

    @Test
    void testDoctorTokenHasRecordingEnabled() {
        JitsiTokenService service = new JitsiTokenService();
        String secret = "telemed_jwt_secret_key_32bytes_long!!";
        ReflectionTestUtils.setField(service, "appId", "telemed-copilot");
        ReflectionTestUtils.setField(service, "secret", secret);
        ReflectionTestUtils.setField(service, "domain", "meet.jit.si");
        ReflectionTestUtils.setField(service, "validitySeconds", 3600L);

        String token = service.generateToken("room-101", "doc-123", "Dr. Silva", "silva@med.com", true);
        assertNotNull(token);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("telemed-copilot", claims.getIssuer());
        assertEquals("room-101", claims.get("room"));
        assertTrue((Boolean) claims.get("moderator"));

        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) claims.get("context");
        @SuppressWarnings("unchecked")
        Map<String, Object> features = (Map<String, Object>) context.get("features");
        assertEquals(true, features.get("recording"));
    }

    @Test
    void testPatientTokenHasRecordingDisabled() {
        JitsiTokenService service = new JitsiTokenService();
        String secret = "telemed_jwt_secret_key_32bytes_long!!";
        ReflectionTestUtils.setField(service, "appId", "telemed-copilot");
        ReflectionTestUtils.setField(service, "secret", secret);
        ReflectionTestUtils.setField(service, "domain", "meet.jit.si");
        ReflectionTestUtils.setField(service, "validitySeconds", 3600L);

        String token = service.generateToken("room-101", "pat-456", "João Silva", "joao@email.com", false);
        assertNotNull(token);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertFalse((Boolean) claims.get("moderator"));

        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) claims.get("context");
        @SuppressWarnings("unchecked")
        Map<String, Object> features = (Map<String, Object>) context.get("features");
        assertEquals(false, features.get("recording"));
    }
}
