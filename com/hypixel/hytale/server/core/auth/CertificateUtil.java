package com.hypixel.hytale.server.core.auth;

import com.hypixel.hytale.logger.HytaleLogger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CertificateUtil {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   @Nullable
   public static String computeCertificateFingerprint(@Nonnull X509Certificate certificate) {
      try {
         MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
         byte[] certBytes = certificate.getEncoded();
         byte[] hash = sha256.digest(certBytes);
         return base64UrlEncode(hash);
      } catch (NoSuchAlgorithmException var4) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var4)).log("SHA-256 algorithm not available");
         return null;
      } catch (CertificateEncodingException var5) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var5)).log("Failed to encode certificate");
         return null;
      }
   }

   public static boolean validateCertificateBinding(@Nullable String jwtFingerprint, @Nullable X509Certificate clientCert) {
      if (jwtFingerprint == null || jwtFingerprint.isEmpty()) {
         LOGGER.at(Level.WARNING).log("JWT missing certificate fingerprint (cnf.x5t#S256) - rejecting token");
         return false;
      } else if (clientCert == null) {
         LOGGER.at(Level.WARNING).log("No client certificate present in mTLS connection - rejecting token");
         return false;
      } else {
         String actualFingerprint = computeCertificateFingerprint(clientCert);
         if (actualFingerprint == null) {
            LOGGER.at(Level.WARNING).log("Failed to compute client certificate fingerprint");
            return false;
         } else {
            boolean matches = timingSafeEquals(jwtFingerprint, actualFingerprint);
            if (!matches) {
               LOGGER.at(Level.WARNING).log("Certificate fingerprint mismatch! JWT: %s, Actual: %s", jwtFingerprint, actualFingerprint);
            } else {
               LOGGER.at(Level.INFO).log("Certificate binding validated successfully");
            }

            return matches;
         }
      }
   }

   public static boolean timingSafeEquals(String a, String b) {
      if (a != null && b != null) {
         byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
         byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
         return MessageDigest.isEqual(aBytes, bBytes);
      } else {
         return a == b;
      }
   }

   private static String base64UrlEncode(byte[] input) {
      String base64 = Base64.getEncoder().encodeToString(input);
      return base64.replace('+', '-').replace('/', '_').replace("=", "");
   }
}
