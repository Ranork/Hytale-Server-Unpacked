package com.hypixel.hytale.server.core.modules.accesscontrol.provider;

import com.hypixel.hytale.server.core.Message;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public interface AccessProvider {
   @Nonnull
   CompletableFuture<Optional<Message>> getDisconnectReason(@Nonnull UUID var1);
}
