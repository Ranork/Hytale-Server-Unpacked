package com.hypixel.hytale.server.core.auth;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DefaultAuthCredentialStore implements IAuthCredentialStore {
   private IAuthCredentialStore.OAuthTokens tokens = new IAuthCredentialStore.OAuthTokens(null, null, null);
   @Nullable
   private UUID profile;

   @Override
   public void setTokens(@Nonnull IAuthCredentialStore.OAuthTokens tokens) {
      this.tokens = tokens;
   }

   @Nonnull
   @Override
   public IAuthCredentialStore.OAuthTokens getTokens() {
      return this.tokens;
   }

   @Override
   public void setProfile(@Nullable UUID uuid) {
      this.profile = uuid;
   }

   @Nullable
   @Override
   public UUID getProfile() {
      return this.profile;
   }

   @Override
   public void clear() {
      this.tokens = new IAuthCredentialStore.OAuthTokens(null, null, null);
      this.profile = null;
   }
}
