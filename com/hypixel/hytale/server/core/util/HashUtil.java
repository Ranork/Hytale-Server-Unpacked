package com.hypixel.hytale.server.core.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.annotation.Nonnull;
import org.bouncycastle.util.encoders.Hex;

public class HashUtil {
   @Nonnull
   public static String sha256(byte[] bytes) {
      try {
         MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
         messageDigest.update(bytes);
         return Hex.toHexString(messageDigest.digest());
      } catch (NoSuchAlgorithmException var2) {
         throw new RuntimeException(var2);
      }
   }
}
