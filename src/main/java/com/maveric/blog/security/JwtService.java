package com.maveric.blog.security;

import com.maveric.blog.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.cglib.core.internal.Function;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtService {

  private static final String SECRET_KEY =
      "467637674e4b277a5d526f485e75712742426b71314b496a5d6265457e";
  private final JwtTokenService jwtTokenService;

  public JwtService(JwtTokenService jwtTokenService) {
    this.jwtTokenService = jwtTokenService;
  }

  public String extractUsername(String jwt) {

    return extractSingleClaims(jwt, Claims::getSubject);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Key getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public <T> T extractSingleClaims(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    extraClaims.put("id", ((User) userDetails).getId()); // Add userId to claims
    return Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000))) // 24 hours
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return username.equals(userDetails.getUsername())
        && !isTokenExpired(token)
        && !jwtTokenService.isTokenInvalid(token);
  }

  private boolean isTokenExpired(String token) {
    return extractExpration(token).before(new Date());
  }

  private Date extractExpration(String token) {
    return extractSingleClaims(token, Claims::getExpiration);
  }

  public Long extractUserId(String jwt) {
    return extractSingleClaims(jwt, claims -> claims.get("id", Long.class));
  }
}
