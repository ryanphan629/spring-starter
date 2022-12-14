package ryan.phan.starter.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Component
public class JwtUtils {

    private static String jwtSecret;
    private static long jwtExpiration;

    private JwtUtils() {
    }

    @Value("${security.jwt.secret_key}")
    public synchronized void setJwtSecret(String jwtSecret) {
        JwtUtils.jwtSecret = jwtSecret;
    }

    @Value("${security.jwt.expiration}")
    public synchronized void setJwtExpiration(long jwtExpiration) {
        JwtUtils.jwtExpiration = jwtExpiration;
    }

    public static String generate(String username) {
        Date now = new Date();
        Date expiration = new Date(System.currentTimeMillis() + jwtExpiration);
        return Jwts.builder()
                .setId(username)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(toSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public static String getUsernameFromJWT(String token) {
        Claims claims = makeJwtParser().parseClaimsJws(token).getBody();
        return claims.getId();
    }

    public static boolean validate(String token) {
        if (Objects.isNull(token)) return false;
        try {
            makeJwtParser().parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty.");
        } catch (SignatureException e) {
            log.error("JWT signature is not match with locally.");
        }
        return false;
    }

    // Make JWT Parser
    private static JwtParser makeJwtParser() {
        return Jwts.parserBuilder().setSigningKey(toSigningKey()).build();
    }

    // Make Sign Key with HMAC
    private static Key toSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
