package com.hypixel.hytale.builtin.adventure.memories.temple;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TempleRespawnPlayersSystem extends DelayedEntitySystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, PlayerRef> playerRefComponentType;
   @Nonnull
   private final ComponentType<EntityStore, TransformComponent> transformComponentType;
   @Nonnull
   private final Query<EntityStore> query;

   public TempleRespawnPlayersSystem(
      @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType, @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType
   ) {
      super(1.0F);
      this.playerRefComponentType = playerRefComponentType;
      this.transformComponentType = transformComponentType;
      this.query = Query.and(playerRefComponentType, transformComponentType);
   }

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      World world = store.getExternalData().getWorld();
      GameplayConfig gameplayConfig = world.getGameplayConfig();
      ForgottenTempleConfig config = gameplayConfig.getPluginConfig().get(ForgottenTempleConfig.class);
      if (config != null) {
         TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         if (!(position.getY() > config.getMinYRespawn())) {
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
            ISpawnProvider spawnProvider = world.getWorldConfig().getSpawnProvider();
            Transform spawnTransform = spawnProvider.getSpawnPoint(ref, commandBuffer);
            Teleport teleportComponent = Teleport.createForPlayer(null, spawnTransform);
            commandBuffer.addComponent(ref, Teleport.getComponentType(), teleportComponent);
            PlayerRef playerRefComponent = archetypeChunk.getComponent(index, this.playerRefComponentType);

            assert playerRefComponent != null;

            SoundUtil.playSoundEvent2dToPlayer(playerRefComponent, config.getRespawnSoundIndex(), SoundCategory.SFX);
         }
      }
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }
}
