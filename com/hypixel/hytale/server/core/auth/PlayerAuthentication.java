package com.hypixel.hytale.server.core.auth;

import com.hypixel.hytale.protocol.HostAddress;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerAuthentication {
   public static final int MAX_REFERRAL_DATA_SIZE = 4096;
   private UUID uuid;
   private String username;
   private byte[] referralData;
   private HostAddress referralSource;

   public PlayerAuthentication() {
   }

   public PlayerAuthentication(@Nonnull UUID uuid, @Nonnull String username) {
      this.uuid = uuid;
      this.username = username;
   }

   @Nonnull
   public String getUsername() {
      if (this.username == null) {
         throw new UnsupportedOperationException("Username not set - incomplete authentication");
      } else {
         return this.username;
      }
   }

   @Nonnull
   public UUID getUuid() {
      if (this.uuid == null) {
         throw new UnsupportedOperationException("UUID not set - incomplete authentication");
      } else {
         return this.uuid;
      }
   }

   public void setUsername(@Nonnull String username) {
      this.username = username;
   }

   public void setUuid(@Nonnull UUID uuid) {
      this.uuid = uuid;
   }

   @Nullable
   public byte[] getReferralData() {
      return this.referralData;
   }

   public void setReferralData(@Nullable byte[] referralData) {
      if (referralData != null && referralData.length > 4096) {
         throw new IllegalArgumentException("Referral data exceeds maximum size of 4096 bytes (got " + referralData.length + ")");
      } else {
         this.referralData = referralData;
      }
   }

   @Nullable
   public HostAddress getReferralSource() {
      return this.referralSource;
   }

   public void setReferralSource(@Nullable HostAddress referralSource) {
      this.referralSource = referralSource;
   }
}
