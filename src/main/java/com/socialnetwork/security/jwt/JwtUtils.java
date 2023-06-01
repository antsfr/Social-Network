package com.socialnetwork.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwtSecret}")
    private String jwtSecret;
    @Value("${jwtExpiration}")
    private long jwtExpirationInMs;

    public String generateJwt(Authentication authentication) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + jwtExpirationInMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public boolean validate(String jwt) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(jwt);
            return true;
        } catch (Exception e) {
            System.err.println("JWT Validation Failed");
        }
        return false;
    }

    public String getUsername(String jwt) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(jwt)
                .getBody().getSubject();
    }
}
