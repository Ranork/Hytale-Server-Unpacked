package com.hypixel.hytale.server.core.auth;

import com.hypixel.hytale.logger.HytaleLogger;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class JWTValidator {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final long CLOCK_SKEW_SECONDS = 300L;
   private static final JWSAlgorithm SUPPORTED_ALGORITHM = JWSAlgorithm.EdDSA;
   private static final int MIN_SIGNATURE_LENGTH = 80;
   private static final int MAX_SIGNATURE_LENGTH = 90;
   private static final Duration JWKS_REFRESH_MIN_INTERVAL = Duration.ofMinutes(5L);
   private final SessionServiceClient sessionServiceClient;
   private final String expectedIssuer;
   private final String expectedAudience;
   private volatile JWKSet cachedJwkSet;
   private final ReentrantLock jwksFetchLock = new ReentrantLock();
   private volatile CompletableFuture<JWKSet> pendingFetch = null;
   private volatile Instant lastJwksRefresh;

   public JWTValidator(@Nonnull SessionServiceClient sessionServiceClient, @Nonnull String expectedIssuer, @Nonnull String expectedAudience) {
      this.sessionServiceClient = sessionServiceClient;
      this.expectedIssuer = expectedIssuer;
      this.expectedAudience = expectedAudience;
   }

   @Nullable
   private static String validateJwtStructure(@Nonnull String token, @Nonnull String tokenType) {
      if (token.isEmpty()) {
         return tokenType + " is empty";
      } else {
         String[] parts = token.split("\\.", -1);
         if (parts.length != 3) {
            return String.format("%s has invalid format (expected 3 parts, got %d)", tokenType, parts.length);
         } else if (parts[2].isEmpty()) {
            return tokenType + " has empty signature - possible signature stripping attack";
         } else {
            int sigLen = parts[2].length();
            return sigLen >= 80 && sigLen <= 90 ? null : String.format("%s has invalid signature length: %d (expected %d-%d)", tokenType, sigLen, 80, 90);
         }
      }
   }

   @Nullable
   public JWTValidator.JWTClaims validateToken(@Nonnull String accessToken, @Nullable X509Certificate clientCert) {
      String structError = validateJwtStructure(accessToken, "Access token");
      if (structError != null) {
         LOGGER.at(Level.WARNING).log(structError);
         return null;
      } else {
         try {
            SignedJWT signedJWT = SignedJWT.parse(accessToken);
            JWSAlgorithm algorithm = signedJWT.getHeader().getAlgorithm();
            if (!SUPPORTED_ALGORITHM.equals(algorithm)) {
               LOGGER.at(Level.WARNING).log("Unsupported JWT algorithm: %s (expected EdDSA)", algorithm);
               return null;
            } else if (!this.verifySignatureWithRetry(signedJWT)) {
               LOGGER.at(Level.WARNING).log("JWT signature verification failed");
               return null;
            } else {
               JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
               JWTValidator.JWTClaims claims = new JWTValidator.JWTClaims();
               claims.issuer = claimsSet.getIssuer();
               claims.audience = claimsSet.getAudience() != null && !claimsSet.getAudience().isEmpty() ? (String)claimsSet.getAudience().get(0) : null;
               claims.subject = claimsSet.getSubject();
               claims.username = claimsSet.getStringClaim("username");
               claims.ipAddress = claimsSet.getStringClaim("ip");
               claims.issuedAt = claimsSet.getIssueTime() != null ? claimsSet.getIssueTime().toInstant().getEpochSecond() : null;
               claims.expiresAt = claimsSet.getExpirationTime() != null ? claimsSet.getExpirationTime().toInstant().getEpochSecond() : null;
               claims.notBefore = claimsSet.getNotBeforeTime() != null ? claimsSet.getNotBeforeTime().toInstant().getEpochSecond() : null;
               Map<String, Object> cnfClaim = claimsSet.getJSONObjectClaim("cnf");
               if (cnfClaim != null) {
                  claims.certificateFingerprint = (String)cnfClaim.get("x5t#S256");
               }

               if (!this.expectedIssuer.equals(claims.issuer)) {
                  LOGGER.at(Level.WARNING).log("Invalid issuer: expected %s, got %s", this.expectedIssuer, claims.issuer);
                  return null;
               } else if (!this.expectedAudience.equals(claims.audience)) {
                  LOGGER.at(Level.WARNING).log("Invalid audience: expected %s, got %s", this.expectedAudience, claims.audience);
                  return null;
               } else {
                  long nowSeconds = Instant.now().getEpochSecond();
                  if (claims.expiresAt == null) {
                     LOGGER.at(Level.WARNING).log("Access token missing expiration claim");
                     return null;
                  } else if (nowSeconds >= claims.expiresAt + 300L) {
                     LOGGER.at(Level.WARNING).log("Token expired (exp: %d, now: %d)", claims.expiresAt, nowSeconds);
                     return null;
                  } else if (claims.notBefore != null && nowSeconds < claims.notBefore - 300L) {
                     LOGGER.at(Level.WARNING).log("Token not yet valid (nbf: %d, now: %d)", claims.notBefore, nowSeconds);
                     return null;
                  } else if (claims.issuedAt != null && claims.issuedAt > nowSeconds + 300L) {
                     LOGGER.at(Level.WARNING).log("Token issued in the future (iat: %d, now: %d)", claims.issuedAt, nowSeconds);
                     return null;
                  } else if (!CertificateUtil.validateCertificateBinding(claims.certificateFingerprint, clientCert)) {
                     LOGGER.at(Level.WARNING).log("Certificate binding validation failed");
                     return null;
                  } else if (claims.getSubjectAsUUID() == null) {
                     LOGGER.at(Level.WARNING).log("Access token has invalid or missing subject UUID");
                     return null;
                  } else {
                     LOGGER.at(Level.INFO).log("JWT validated successfully for user %s (UUID: %s)", claims.username, claims.subject);
                     return claims;
                  }
               }
            }
         } catch (ParseException var11) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var11)).log("Failed to parse JWT");
            return null;
         } catch (Exception var12) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var12)).log("JWT validation error");
            return null;
         }
      }
   }

   private boolean verifySignature(SignedJWT signedJWT, JWKSet jwkSet) {
      try {
         String keyId = signedJWT.getHeader().getKeyID();
         OctetKeyPair ed25519Key = null;

         for (JWK jwk : jwkSet.getKeys()) {
            if (jwk instanceof OctetKeyPair okp && (keyId == null || keyId.equals(jwk.getKeyID()))) {
               ed25519Key = okp;
               break;
            }
         }

         if (ed25519Key == null) {
            LOGGER.at(Level.WARNING).log("No Ed25519 key found for kid=%s", keyId);
            return false;
         } else {
            Ed25519Verifier verifier = new Ed25519Verifier(ed25519Key);
            boolean valid = signedJWT.verify(verifier);
            if (valid) {
               LOGGER.at(Level.FINE).log("JWT signature verified with key kid=%s", keyId);
            }

            return valid;
         }
      } catch (Exception var8) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var8)).log("JWT signature verification failed");
         return false;
      }
   }

   @Nullable
   private JWKSet getJwkSet() {
      return this.getJwkSet(false);
   }

   @Nullable
   private JWKSet getJwkSet(boolean forceRefresh) {
      if (!forceRefresh && this.cachedJwkSet != null) {
         return this.cachedJwkSet;
      } else {
         this.jwksFetchLock.lock();

         JWKSet var3;
         try {
            if (!forceRefresh && this.cachedJwkSet != null) {
               return this.cachedJwkSet;
            }

            CompletableFuture<JWKSet> existing = this.pendingFetch;
            if (existing == null || existing.isDone()) {
               if (forceRefresh) {
                  LOGGER.at(Level.INFO).log("Force refreshing JWKS cache (key rotation or verification failure)");
               }

               this.pendingFetch = CompletableFuture.supplyAsync(this::fetchJwksFromService);
               return this.pendingFetch.join();
            }

            this.jwksFetchLock.unlock();

            try {
               var3 = existing.join();
            } finally {
               this.jwksFetchLock.lock();
            }
         } finally {
            this.jwksFetchLock.unlock();
         }

         return var3;
      }
   }

   @Nullable
   private JWKSet fetchJwksFromService() {
      SessionServiceClient.JwksResponse jwksResponse = this.sessionServiceClient.getJwks();
      if (jwksResponse != null && jwksResponse.keys != null && jwksResponse.keys.length != 0) {
         try {
            ArrayList<JWK> jwkList = new ArrayList<>();

            for (SessionServiceClient.JwkKey key : jwksResponse.keys) {
               JWK jwk = this.convertToJWK(key);
               if (jwk != null) {
                  jwkList.add(jwk);
               }
            }

            if (jwkList.isEmpty()) {
               LOGGER.at(Level.WARNING).log("No valid JWKs found in JWKS response");
               return this.cachedJwkSet;
            } else {
               JWKSet newSet = new JWKSet(jwkList);
               this.cachedJwkSet = newSet;
               this.lastJwksRefresh = Instant.now();
               LOGGER.at(Level.INFO).log("JWKS loaded with %d keys (cached permanently)", jwkList.size());
               return newSet;
            }
         } catch (Exception var8) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var8)).log("Failed to parse JWKS");
            return this.cachedJwkSet;
         }
      } else {
         LOGGER.at(Level.WARNING).log("Failed to fetch JWKS or no keys available");
         return this.cachedJwkSet;
      }
   }

   private boolean verifySignatureWithRetry(SignedJWT signedJWT) {
      JWKSet jwkSet = this.getJwkSet();
      if (jwkSet == null) {
         return false;
      } else if (this.verifySignature(signedJWT, jwkSet)) {
         return true;
      } else if (!this.canForceRefreshJwks()) {
         LOGGER.at(Level.FINE).log("Signature verification failed but JWKS was refreshed recently; skipping refresh");
         return false;
      } else {
         LOGGER.at(Level.INFO).log("Signature verification failed with cached JWKS, retrying with fresh keys");
         JWKSet freshJwkSet = this.getJwkSet(true);
         return freshJwkSet != null && freshJwkSet != jwkSet ? this.verifySignature(signedJWT, freshJwkSet) : false;
      }
   }

   private boolean canForceRefreshJwks() {
      Instant lastRefresh = this.lastJwksRefresh;
      return lastRefresh == null ? true : Duration.between(lastRefresh, Instant.now()).compareTo(JWKS_REFRESH_MIN_INTERVAL) >= 0;
   }

   @Nullable
   private JWK convertToJWK(SessionServiceClient.JwkKey key) {
      if (!"OKP".equals(key.kty)) {
         LOGGER.at(Level.WARNING).log("Unsupported key type: %s (expected OKP)", key.kty);
         return null;
      } else {
         try {
            String json = String.format("{\"kty\":\"OKP\",\"crv\":\"%s\",\"x\":\"%s\",\"kid\":\"%s\",\"alg\":\"EdDSA\"}", key.crv, key.x, key.kid);
            return JWK.parse(json);
         } catch (Exception var3) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var3)).log("Failed to parse Ed25519 key");
            return null;
         }
      }
   }

   public void invalidateJwksCache() {
      this.jwksFetchLock.lock();

      try {
         this.cachedJwkSet = null;
         this.pendingFetch = null;
      } finally {
         this.jwksFetchLock.unlock();
      }
   }

   @Nullable
   public JWTValidator.IdentityTokenClaims validateIdentityToken(@Nonnull String identityToken) {
      String structError = validateJwtStructure(identityToken, "Identity token");
      if (structError != null) {
         LOGGER.at(Level.WARNING).log(structError);
         return null;
      } else {
         try {
            SignedJWT signedJWT = SignedJWT.parse(identityToken);
            JWSAlgorithm algorithm = signedJWT.getHeader().getAlgorithm();
            if (!SUPPORTED_ALGORITHM.equals(algorithm)) {
               LOGGER.at(Level.WARNING).log("Unsupported identity token algorithm: %s (expected EdDSA)", algorithm);
               return null;
            } else if (!this.verifySignatureWithRetry(signedJWT)) {
               LOGGER.at(Level.WARNING).log("Identity token signature verification failed");
               return null;
            } else {
               JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
               JWTValidator.IdentityTokenClaims claims = new JWTValidator.IdentityTokenClaims();
               claims.issuer = claimsSet.getIssuer();
               claims.subject = claimsSet.getSubject();
               claims.issuedAt = claimsSet.getIssueTime() != null ? claimsSet.getIssueTime().toInstant().getEpochSecond() : null;
               claims.expiresAt = claimsSet.getExpirationTime() != null ? claimsSet.getExpirationTime().toInstant().getEpochSecond() : null;
               claims.notBefore = claimsSet.getNotBeforeTime() != null ? claimsSet.getNotBeforeTime().toInstant().getEpochSecond() : null;
               claims.scope = claimsSet.getStringClaim("scope");
               Map<String, Object> profile = claimsSet.getJSONObjectClaim("profile");
               if (profile != null) {
                  claims.username = (String)profile.get("username");
                  claims.skin = (String)profile.get("skin");
                  if (profile.get("entitlements") instanceof List<?> list) {
                     claims.entitlements = list.stream().filter(String.class::isInstance).map(String.class::cast).toArray(String[]::new);
                  }
               }

               if (!this.expectedIssuer.equals(claims.issuer)) {
                  LOGGER.at(Level.WARNING).log("Invalid identity token issuer: expected %s, got %s", this.expectedIssuer, claims.issuer);
                  return null;
               } else {
                  long nowSeconds = Instant.now().getEpochSecond();
                  if (claims.expiresAt == null) {
                     LOGGER.at(Level.WARNING).log("Identity token missing expiration claim");
                     return null;
                  } else if (nowSeconds >= claims.expiresAt + 300L) {
                     LOGGER.at(Level.WARNING).log("Identity token expired (exp: %d, now: %d)", claims.expiresAt, nowSeconds);
                     return null;
                  } else if (claims.notBefore != null && nowSeconds < claims.notBefore - 300L) {
                     LOGGER.at(Level.WARNING).log("Identity token not yet valid (nbf: %d, now: %d)", claims.notBefore, nowSeconds);
                     return null;
                  } else if (claims.issuedAt != null && claims.issuedAt > nowSeconds + 300L) {
                     LOGGER.at(Level.WARNING).log("Identity token issued in the future (iat: %d, now: %d)", claims.issuedAt, nowSeconds);
                     return null;
                  } else if (claims.getSubjectAsUUID() == null) {
                     LOGGER.at(Level.WARNING).log("Identity token has invalid or missing subject UUID");
                     return null;
                  } else {
                     LOGGER.at(Level.INFO).log("Identity token validated successfully for user %s (UUID: %s)", claims.username, claims.subject);
                     return claims;
                  }
               }
            }
         } catch (ParseException var10) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var10)).log("Failed to parse identity token");
            return null;
         } catch (Exception var11) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var11)).log("Identity token validation error");
            return null;
         }
      }
   }

   @Nullable
   public JWTValidator.SessionTokenClaims validateSessionToken(@Nonnull String sessionToken) {
      String structError = validateJwtStructure(sessionToken, "Session token");
      if (structError != null) {
         LOGGER.at(Level.WARNING).log(structError);
         return null;
      } else {
         try {
            SignedJWT signedJWT = SignedJWT.parse(sessionToken);
            JWSAlgorithm algorithm = signedJWT.getHeader().getAlgorithm();
            if (!SUPPORTED_ALGORITHM.equals(algorithm)) {
               LOGGER.at(Level.WARNING).log("Unsupported session token algorithm: %s (expected EdDSA)", algorithm);
               return null;
            } else if (!this.verifySignatureWithRetry(signedJWT)) {
               LOGGER.at(Level.WARNING).log("Session token signature verification failed");
               return null;
            } else {
               JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
               JWTValidator.SessionTokenClaims claims = new JWTValidator.SessionTokenClaims();
               claims.issuer = claimsSet.getIssuer();
               claims.subject = claimsSet.getSubject();
               claims.issuedAt = claimsSet.getIssueTime() != null ? claimsSet.getIssueTime().toInstant().getEpochSecond() : null;
               claims.expiresAt = claimsSet.getExpirationTime() != null ? claimsSet.getExpirationTime().toInstant().getEpochSecond() : null;
               claims.notBefore = claimsSet.getNotBeforeTime() != null ? claimsSet.getNotBeforeTime().toInstant().getEpochSecond() : null;
               claims.scope = claimsSet.getStringClaim("scope");
               if (!this.expectedIssuer.equals(claims.issuer)) {
                  LOGGER.at(Level.WARNING).log("Invalid session token issuer: expected %s, got %s", this.expectedIssuer, claims.issuer);
                  return null;
               } else {
                  long nowSeconds = Instant.now().getEpochSecond();
                  if (claims.expiresAt == null) {
                     LOGGER.at(Level.WARNING).log("Session token missing expiration claim");
                     return null;
                  } else if (nowSeconds >= claims.expiresAt + 300L) {
                     LOGGER.at(Level.WARNING).log("Session token expired (exp: %d, now: %d)", claims.expiresAt, nowSeconds);
                     return null;
                  } else if (claims.notBefore != null && nowSeconds < claims.notBefore - 300L) {
                     LOGGER.at(Level.WARNING).log("Session token not yet valid (nbf: %d, now: %d)", claims.notBefore, nowSeconds);
                     return null;
                  } else if (claims.issuedAt != null && claims.issuedAt > nowSeconds + 300L) {
                     LOGGER.at(Level.WARNING).log("Session token issued in the future (iat: %d, now: %d)", claims.issuedAt, nowSeconds);
                     return null;
                  } else if (claims.getSubjectAsUUID() == null) {
                     LOGGER.at(Level.WARNING).log("Session token has invalid or missing subject UUID");
                     return null;
                  } else {
                     LOGGER.at(Level.INFO).log("Session token validated successfully (UUID: %s)", claims.subject);
                     return claims;
                  }
               }
            }
         } catch (ParseException var9) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var9)).log("Failed to parse session token");
            return null;
         } catch (Exception var10) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var10)).log("Session token validation error");
            return null;
         }
      }
   }

   public static class IdentityTokenClaims {
      public String issuer;
      public String subject;
      public String username;
      public String[] entitlements;
      public String skin;
      public Long issuedAt;
      public Long expiresAt;
      public Long notBefore;
      public String scope;

      @Nullable
      public UUID getSubjectAsUUID() {
         if (this.subject == null) {
            return null;
         } else {
            try {
               return UUID.fromString(this.subject);
            } catch (IllegalArgumentException var2) {
               return null;
            }
         }
      }

      @Nonnull
      public String[] getScopes() {
         return this.scope != null && !this.scope.isEmpty() ? this.scope.split(" ") : new String[0];
      }

      public boolean hasScope(@Nonnull String targetScope) {
         for (String s : this.getScopes()) {
            if (s.equals(targetScope)) {
               return true;
            }
         }

         return false;
      }
   }

   public static class JWTClaims {
      public String issuer;
      public String audience;
      public String subject;
      public String username;
      public String ipAddress;
      public Long issuedAt;
      public Long expiresAt;
      public Long notBefore;
      public String certificateFingerprint;

      @Nullable
      public UUID getSubjectAsUUID() {
         if (this.subject == null) {
            return null;
         } else {
            try {
               return UUID.fromString(this.subject);
            } catch (IllegalArgumentException var2) {
               return null;
            }
         }
      }
   }

   public static class SessionTokenClaims {
      public String issuer;
      public String subject;
      public Long issuedAt;
      public Long expiresAt;
      public Long notBefore;
      public String scope;

      @Nullable
      public UUID getSubjectAsUUID() {
         if (this.subject == null) {
            return null;
         } else {
            try {
               return UUID.fromString(this.subject);
            } catch (IllegalArgumentException var2) {
               return null;
            }
         }
      }

      @Nonnull
      public String[] getScopes() {
         return this.scope != null && !this.scope.isEmpty() ? this.scope.split(" ") : new String[0];
      }

      public boolean hasScope(@Nonnull String targetScope) {
         for (String s : this.getScopes()) {
            if (s.equals(targetScope)) {
               return true;
            }
         }

         return false;
      }
   }
}
