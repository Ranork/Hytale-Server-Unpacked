package com.hypixel.hytale.server.core.asset.type.gameplay.respawn;

import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public interface RespawnController {
   @Nonnull
   CodecMapCodec<RespawnController> CODEC = new CodecMapCodec<>("Type");

   CompletableFuture<Void> respawnPlayer(@Nonnull World var1, @Nonnull Ref<EntityStore> var2, @Nonnull ComponentAccessor<EntityStore> var3);
}
