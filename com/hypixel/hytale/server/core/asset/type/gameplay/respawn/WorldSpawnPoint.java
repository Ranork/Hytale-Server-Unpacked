package com.hypixel.hytale.server.core.asset.type.gameplay.respawn;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class WorldSpawnPoint implements RespawnController {
   public static final WorldSpawnPoint INSTANCE = new WorldSpawnPoint();
   @Nonnull
   public static final BuilderCodec<WorldSpawnPoint> CODEC = BuilderCodec.builder(WorldSpawnPoint.class, () -> INSTANCE).build();

   @Override
   public CompletableFuture<Void> respawnPlayer(
      @Nonnull World world, @Nonnull Ref<EntityStore> playerReference, @Nonnull ComponentAccessor<EntityStore> commandBuffer
   ) {
      ISpawnProvider spawnProvider = world.getWorldConfig().getSpawnProvider();

      assert spawnProvider != null;

      Transform spawnPoint = spawnProvider.getSpawnPoint(playerReference, commandBuffer);
      Teleport teleportComponent = Teleport.createForPlayer(spawnPoint);
      commandBuffer.addComponent(playerReference, Teleport.getComponentType(), teleportComponent);
      return CompletableFuture.completedFuture(null);
   }
}
