package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.component.system.tick.RunWhenPausedSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class PlayerSavingSystems {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final float PLAYER_SAVE_INTERVAL_SECONDS = 10.0F;

   public static class SaveDataResource implements Resource<EntityStore> {
      private float delay = 10.0F;

      @Nonnull
      @Override
      public Resource<EntityStore> clone() {
         PlayerSavingSystems.SaveDataResource data = new PlayerSavingSystems.SaveDataResource();
         data.delay = this.delay;
         return data;
      }
   }

   public static class TickingSystem extends EntityTickingSystem<EntityStore> implements RunWhenPausedSystem<EntityStore> {
      @Nonnull
      private final ResourceType<EntityStore, PlayerSavingSystems.SaveDataResource> dataResourceType = this.registerResource(
         PlayerSavingSystems.SaveDataResource.class, PlayerSavingSystems.SaveDataResource::new
      );
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType;
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      @Nonnull
      private final ComponentType<EntityStore, HeadRotation> headRotationComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public TickingSystem(@Nonnull ComponentType<EntityStore, Player> playerComponentType) {
         this.playerComponentType = playerComponentType;
         this.transformComponentType = TransformComponent.getComponentType();
         this.headRotationComponentType = HeadRotation.getComponentType();
         this.query = Archetype.of(playerComponentType, this.transformComponentType, this.headRotationComponentType);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      @Override
      public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
         World world = store.getExternalData().getWorld();
         if (world.getWorldConfig().isSavingPlayers()) {
            PlayerSavingSystems.SaveDataResource data = store.getResource(this.dataResourceType);
            data.delay -= dt;
            if (data.delay <= 0.0F) {
               data.delay = 10.0F;
               store.tick(this, dt, systemIndex);
            }
         }
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Player playerComponent = archetypeChunk.getComponent(index, this.playerComponentType);

         assert playerComponent != null;

         TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

         assert transformComponent != null;

         HeadRotation headRotationComponent = archetypeChunk.getComponent(index, this.headRotationComponentType);

         assert headRotationComponent != null;

         PlayerConfigData data = playerComponent.getPlayerConfigData();
         Vector3d position = transformComponent.getPosition();
         Vector3f rotation = headRotationComponent.getRotation();
         Vector3d lastSavedPosition = data.lastSavedPosition;
         Vector3f lastSavedRotation = data.lastSavedRotation;
         if (!lastSavedPosition.equals(position)
            || !lastSavedRotation.equals(rotation)
            || data.consumeHasChanged()
            || playerComponent.getInventory().consumeNeedsSaving()) {
            lastSavedPosition.assign(position);
            lastSavedRotation.assign(rotation);
            playerComponent.saveConfig(store.getExternalData().getWorld(), EntityUtils.toHolder(index, archetypeChunk));
         }
      }
   }

   public static class WorldRemovedSystem extends StoreSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType;
      @Nonnull
      private final ComponentType<EntityStore, PlayerRef> refComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public WorldRemovedSystem(@Nonnull ComponentType<EntityStore, Player> playerComponentType) {
         this.playerComponentType = playerComponentType;
         this.refComponentType = PlayerRef.getComponentType();
         this.query = Query.and(playerComponentType, this.refComponentType);
      }

      @Override
      public void onSystemAddedToStore(@Nonnull Store<EntityStore> store) {
      }

      @Override
      public void onSystemRemovedFromStore(@Nonnull Store<EntityStore> store) {
         if (store.getExternalData().getWorld().getWorldConfig().isSavingPlayers()) {
            PlayerSavingSystems.LOGGER.at(Level.INFO).log("Saving Players...");
         } else {
            PlayerSavingSystems.LOGGER.at(Level.INFO).log("Disconnecting Players...");
         }

         store.forEachEntityParallel(this.query, (index, archetypeChunk, commandBuffer) -> {
            Player playerComponent = archetypeChunk.getComponent(index, this.playerComponentType);

            assert playerComponent != null;

            PlayerRef playerRefComponent = archetypeChunk.getComponent(index, this.refComponentType);

            assert playerRefComponent != null;

            World world = commandBuffer.getExternalData().getWorld();
            if (world.getWorldConfig().isSavingPlayers()) {
               playerComponent.saveConfig(world, EntityUtils.toHolder(index, archetypeChunk));
            }

            playerRefComponent.getPacketHandler().disconnect("Stopping world!");
         });
      }
   }
}
