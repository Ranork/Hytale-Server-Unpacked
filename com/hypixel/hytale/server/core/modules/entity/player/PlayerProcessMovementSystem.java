package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.CollisionResultComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PositionDataComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class PlayerProcessMovementSystem extends EntityTickingSystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, Player> playerComponentType;
   @Nonnull
   private final ComponentType<EntityStore, BoundingBox> boundingBoxComponentType;
   @Nonnull
   private final ComponentType<EntityStore, Velocity> velocityComponentType;
   @Nonnull
   private final ComponentType<EntityStore, CollisionResultComponent> collisionResultComponentType;
   @Nonnull
   private final ComponentType<EntityStore, PlayerRef> playerRefComponentType;
   @Nonnull
   private final ComponentType<EntityStore, TransformComponent> transformComponentType;
   @Nonnull
   private final ComponentType<EntityStore, PositionDataComponent> positionDataComponentType;
   @Nonnull
   private final Query<EntityStore> query;

   public PlayerProcessMovementSystem(
      @Nonnull ComponentType<EntityStore, Player> playerComponentType,
      @Nonnull ComponentType<EntityStore, Velocity> velocityComponentType,
      @Nonnull ComponentType<EntityStore, CollisionResultComponent> collisionResultComponentType
   ) {
      this.playerComponentType = playerComponentType;
      this.velocityComponentType = velocityComponentType;
      this.collisionResultComponentType = collisionResultComponentType;
      this.boundingBoxComponentType = BoundingBox.getComponentType();
      this.playerRefComponentType = PlayerRef.getComponentType();
      this.transformComponentType = TransformComponent.getComponentType();
      this.positionDataComponentType = PositionDataComponent.getComponentType();
      this.query = Query.and(
         playerComponentType,
         this.playerRefComponentType,
         this.transformComponentType,
         this.boundingBoxComponentType,
         velocityComponentType,
         collisionResultComponentType,
         this.positionDataComponentType
      );
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }

   @Override
   public boolean isParallel(int archetypeChunkSize, int taskCount) {
      return false;
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
      Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
      Player playerComponent = archetypeChunk.getComponent(index, this.playerComponentType);

      assert playerComponent != null;

      Velocity velocityComponent = archetypeChunk.getComponent(index, this.velocityComponentType);

      assert velocityComponent != null;

      CollisionResultComponent collisionResultComponent = archetypeChunk.getComponent(index, this.collisionResultComponentType);

      assert collisionResultComponent != null;

      InteractionManager interactionManagerComponent = archetypeChunk.getComponent(index, InteractionModule.get().getInteractionManagerComponent());
      if (interactionManagerComponent != null) {
         PlayerRef playerRefComponent = archetypeChunk.getComponent(index, this.playerRefComponentType);

         assert playerRefComponent != null;

         TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

         assert transformComponent != null;

         boolean pendingCollisionCheck = collisionResultComponent.isPendingCollisionCheck();
         collisionResultComponent.getCollisionStartPositionCopy()
            .assign(pendingCollisionCheck ? collisionResultComponent.getCollisionStartPosition() : transformComponent.getPosition());
         collisionResultComponent.getCollisionPositionOffsetCopy().assign(collisionResultComponent.getCollisionPositionOffset());
         collisionResultComponent.resetLocationChange();
         if (collisionResultComponent.getCollisionPositionOffsetCopy().squaredLength() >= 100.0) {
            if (playerComponent.getGameMode() == GameMode.Adventure) {
               Entity.LOGGER
                  .at(Level.WARNING)
                  .log(
                     "%s, %s: Jump in location in processMovementBlockCollisions %s",
                     playerRefComponent.getUsername(),
                     playerRefComponent.getUuid(),
                     collisionResultComponent.getCollisionPositionOffsetCopy().length()
                  );
            }

            playerComponent.resetVelocity(velocityComponent);
         } else {
            BoundingBox boundingBoxComponent = archetypeChunk.getComponent(index, this.boundingBoxComponentType);

            assert boundingBoxComponent != null;

            Box boundingBox = boundingBoxComponent.getBoundingBox();
            if (pendingCollisionCheck) {
            }

            CollisionModule.get()
               .findIntersections(
                  world, boundingBox, collisionResultComponent.getCollisionStartPositionCopy(), collisionResultComponent.getCollisionResult(), true, false
               );
            playerComponent.processVelocitySample(dt, collisionResultComponent.getCollisionPositionOffsetCopy(), velocityComponent);
            Ref<ChunkStore> chunkRef = transformComponent.getChunkRef();
            if (chunkRef != null && chunkRef.isValid()) {
               Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
               WorldChunk worldChunkComponent = chunkStore.getComponent(chunkRef, WorldChunk.getComponentType());

               assert worldChunkComponent != null;

               PositionDataComponent positionDataComponent = archetypeChunk.getComponent(index, this.positionDataComponentType);

               assert positionDataComponent != null;

               Vector3i blockPosition = transformComponent.getPosition().toVector3i();
               positionDataComponent.setInsideBlockTypeId(worldChunkComponent.getBlock(blockPosition));
               positionDataComponent.setStandingOnBlockTypeId(worldChunkComponent.getBlock(blockPosition.x, blockPosition.y - 1, blockPosition.z));
            }

            commandBuffer.run(
               _store -> {
                  int damageToEntity = collisionResultComponent.getCollisionResult()
                     .defaultTriggerBlocksProcessing(interactionManagerComponent, playerComponent, ref, playerComponent.executeTriggers, commandBuffer);
                  if (playerComponent.executeBlockDamage && damageToEntity > 0) {
                     Damage damage = new Damage(Damage.NULL_SOURCE, DamageCause.ENVIRONMENT, damageToEntity);
                     DamageSystems.executeDamage(index, archetypeChunk, commandBuffer, damage);
                  }
               }
            );
         }
      }
   }
}
