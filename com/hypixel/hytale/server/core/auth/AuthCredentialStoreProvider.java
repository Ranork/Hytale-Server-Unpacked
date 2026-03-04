package com.hypixel.hytale.server.core.auth;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;
import javax.annotation.Nonnull;

public interface AuthCredentialStoreProvider {
   BuilderCodecMapCodec<AuthCredentialStoreProvider> CODEC = new BuilderCodecMapCodec<>("Type", true);

   @Nonnull
   IAuthCredentialStore createStore();
}
