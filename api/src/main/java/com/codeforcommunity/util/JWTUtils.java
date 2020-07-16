package com.codeforcommunity.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.vertx.core.http.HttpServerRequest;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class JWTUtils {

  private final String JWT_KEY;
  private final long TOKEN_DURATION;

  public JWTUtils() {
    this("SECRET_KEY", 3600000);
  }

  public JWTUtils(String secret, long duration) {
    this.JWT_KEY = secret;
    this.TOKEN_DURATION = duration;
  }

  public long getDuration() {
    return TOKEN_DURATION;
  }

  public String createJWT(String issuer, String subject, int userId, boolean isAdmin) {
    // The JWT signature algorithm we will be using to sign the token
    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    long nowMillis = System.currentTimeMillis();
    Date now = new Date(nowMillis);

    // We will sign our JWT with our ApiKey secret
    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(JWT_KEY);
    Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

    // Let's set the JWT Claims
    JwtBuilder builder =
        Jwts.builder()
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(now)
            .setSubject(subject)
            .setIssuer(issuer)
            .claim("userId", userId)
            .claim("isAdmin", isAdmin)
            .signWith(signatureAlgorithm, signingKey);

    // if it has been specified, let's add the expiration
    long expMillis = nowMillis + TOKEN_DURATION;
    Date exp = new Date(expMillis);
    builder.setExpiration(exp);

    // Builds the JWT and serializes it to a compact, URL-safe string
    return builder.compact();
  }

  public int getUserId(HttpServerRequest request) {
    try {
      return (int) getClaims(request).get("userId");
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  public Claims getClaims(HttpServerRequest request) {
    String jwt = request.headers().get("Authorization");
    boolean isNullOrEmpty = jwt == null || jwt.isEmpty();

    if (isNullOrEmpty) return null;

    Claims c = decodeJWT(jwt.split(" ")[1]);

    return c;
  }

  public Claims decodeJWT(String jwt) {
    try {
      return Jwts.parser()
          .setSigningKey(DatatypeConverter.parseBase64Binary(JWT_KEY))
          .parseClaimsJws(jwt)
          .getBody();
    } catch (Exception e) {
      return null;
    }
  }
}
