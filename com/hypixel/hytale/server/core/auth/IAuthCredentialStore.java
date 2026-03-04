package com.hypixel.hytale.server.core.auth;

import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IAuthCredentialStore {
   void setTokens(@Nonnull IAuthCredentialStore.OAuthTokens var1);

   @Nonnull
   IAuthCredentialStore.OAuthTokens getTokens();

   void setProfile(@Nullable UUID var1);

   @Nullable
   UUID getProfile();

   void clear();

   public record OAuthTokens(@Nullable String accessToken, @Nullable String refreshToken, @Nullable Instant accessTokenExpiresAt) {
      public boolean isValid() {
         return this.refreshToken != null;
      }
   }
}
