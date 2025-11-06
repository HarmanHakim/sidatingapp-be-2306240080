package io.harman.sidating_app_be.security.jwt;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${sidating-app-be.app.jwtSecret}")
    private String jwtSecret;

    @Value("${sidating-app-be.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String generateJwtToken(String id, String username, String email, String name, String role) {
        return Jwts.builder()
            .subject(username)
            .claim("id", id)
            .claim("email", email)
            .claim("name", name)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
            .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
            .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        JwtParser jwtParser = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())).build();
        Claims claims = jwtParser.parse(token).accept(Jws.CLAIMS).getPayload();
        return claims.getSubject();
    }

    public String getIdFromJwtToken(String token) {
        JwtParser jwtParser = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())).build();
        Claims claims = jwtParser.parse(token).accept(Jws.CLAIMS).getPayload();
        return claims.get("id", String.class);
    }

    public String getEmailFromJwtToken(String token) {
        JwtParser jwtParser = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())).build();
        Claims claims = jwtParser.parse(token).accept(Jws.CLAIMS).getPayload();
        return claims.get("email", String.class);
    }

    public String getNameFromJwtToken(String token) {
        JwtParser jwtParser = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())).build();
        Claims claims = jwtParser.parse(token).accept(Jws.CLAIMS).getPayload();
        return claims.get("name", String.class);
    }

    public String getRoleFromJwtToken(String token) {
        JwtParser jwtParser = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())).build();
        Claims claims = jwtParser.parse(token).accept(Jws.CLAIMS).getPayload();
        return claims.get("role", String.class);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())).build().parse(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        }
        return false;
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            return ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
        }
        return null;
    }
}
