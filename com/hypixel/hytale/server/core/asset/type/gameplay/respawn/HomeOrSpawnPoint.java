package com.hypixel.hytale.server.core.asset.type.gameplay.respawn;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class HomeOrSpawnPoint implements RespawnController {
   @Nonnull
   public static final HomeOrSpawnPoint INSTANCE = new HomeOrSpawnPoint();
   @Nonnull
   public static final BuilderCodec<HomeOrSpawnPoint> CODEC = BuilderCodec.builder(HomeOrSpawnPoint.class, () -> INSTANCE).build();

   @Override
   public CompletableFuture<Void> respawnPlayer(
      @Nonnull World world, @Nonnull Ref<EntityStore> playerReference, @Nonnull ComponentAccessor<EntityStore> commandBuffer
   ) {
      return Player.getRespawnPosition(playerReference, world.getName(), commandBuffer).thenAcceptAsync(homeTransform -> {
         if (playerReference.isValid()) {
            Teleport teleportComponent = Teleport.createForPlayer(homeTransform);
            playerReference.getStore().addComponent(playerReference, Teleport.getComponentType(), teleportComponent);
         }
      }, world);
   }
}
