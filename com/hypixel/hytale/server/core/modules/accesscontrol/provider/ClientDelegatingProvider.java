package com.hypixel.hytale.server.core.modules.accesscontrol.provider;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class ClientDelegatingProvider implements AccessProvider {
   @Nonnull
   @Override
   public CompletableFuture<Optional<String>> getDisconnectReason(UUID uuid) {
      return CompletableFuture.completedFuture(Optional.empty());
   }
}
