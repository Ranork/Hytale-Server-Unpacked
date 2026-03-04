package com.hypixel.hytale.server.core.auth;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class MemoryAuthCredentialStoreProvider implements AuthCredentialStoreProvider {
   public static final String ID = "Memory";
   public static final BuilderCodec<MemoryAuthCredentialStoreProvider> CODEC = BuilderCodec.builder(
         MemoryAuthCredentialStoreProvider.class, MemoryAuthCredentialStoreProvider::new
      )
      .build();

   @Nonnull
   @Override
   public IAuthCredentialStore createStore() {
      return new DefaultAuthCredentialStore();
   }

   @Nonnull
   @Override
   public String toString() {
      return "MemoryAuthCredentialStoreProvider{}";
   }
}
