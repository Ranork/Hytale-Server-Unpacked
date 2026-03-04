package com.hypixel.hytale.server.core.modules.entity;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.DisableProcessingAssert;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.projectile.config.Projectile;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class LegacyProjectileSystems {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public static class OnAddHolderSystem extends HolderSystem<EntityStore> {
      @Nonnull
      private static final ComponentType<EntityStore, ProjectileComponent> PROJECTILE_COMPONENT_TYPE = ProjectileComponent.getComponentType();

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         if (!holder.getArchetype().contains(NetworkId.getComponentType())) {
            holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
         }

         ProjectileComponent projectileComponent = holder.getComponent(PROJECTILE_COMPONENT_TYPE);

         assert projectileComponent != null;

         projectileComponent.initialize();
         ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(projectileComponent.getAppearance());
         BoundingBox boundingBox;
         if (modelAsset != null) {
            Model model = Model.createUnitScaleModel(modelAsset);
            holder.putComponent(ModelComponent.getComponentType(), new ModelComponent(model));
            holder.putComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
            boundingBox = new BoundingBox(model.getBoundingBox());
         } else {
            Projectile projectileAsset = projectileComponent.getProjectile();
            if (projectileAsset != null) {
               boundingBox = new BoundingBox(Box.horizontallyCentered(projectileAsset.getRadius(), projectileAsset.getHeight(), projectileAsset.getRadius()));
            } else {
               boundingBox = new BoundingBox(Box.horizontallyCentered(0.25, 0.25, 0.25));
            }
         }

         holder.putComponent(BoundingBox.getComponentType(), boundingBox);
         projectileComponent.initializePhysics(boundingBox);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Override
      public Query<EntityStore> getQuery() {
         return PROJECTILE_COMPONENT_TYPE;
      }
   }

   public static class OnAddRefSystem extends RefSystem<EntityStore> {
      @Nonnull
      private static final ComponentType<EntityStore, ProjectileComponent> PROJECTILE_COMPONENT_TYPE = ProjectileComponent.getComponentType();

      @Override
      public Query<EntityStore> getQuery() {
         return PROJECTILE_COMPONENT_TYPE;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         ProjectileComponent projectileComponent = commandBuffer.getComponent(ref, PROJECTILE_COMPONENT_TYPE);

         assert projectileComponent != null;

         if (projectileComponent.getProjectile() == null) {
            LegacyProjectileSystems.LOGGER.at(Level.WARNING).log("Removing projectile entity %s as it failed to initialize correctly!", ref);
            commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }

   public static class TickingSystem extends EntityTickingSystem<EntityStore> implements DisableProcessingAssert {
      @Nonnull
      private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      @Nonnull
      private final ComponentType<EntityStore, Velocity> velocityComponentType;
      @Nonnull
      private final ComponentType<EntityStore, BoundingBox> boundingBoxComponentType;
      @Nonnull
      private final ComponentType<EntityStore, ProjectileComponent> projectileComponentType;
      @Nonnull
      private final Archetype<EntityStore> archetype;

      public TickingSystem(
         @Nonnull ComponentType<EntityStore, ProjectileComponent> projectileComponentType,
         @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType,
         @Nonnull ComponentType<EntityStore, Velocity> velocityComponentType,
         @Nonnull ComponentType<EntityStore, BoundingBox> boundingBoxComponentType
      ) {
         this.projectileComponentType = projectileComponentType;
         this.velocityComponentType = velocityComponentType;
         this.boundingBoxComponentType = boundingBoxComponentType;
         this.transformComponentType = transformComponentType;
         this.archetype = Archetype.of(projectileComponentType, transformComponentType, velocityComponentType, boundingBoxComponentType);
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.archetype;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

         assert transformComponent != null;

         ProjectileComponent projectileComponent = archetypeChunk.getComponent(index, this.projectileComponentType);

         assert projectileComponent != null;

         Velocity velocityComponent = archetypeChunk.getComponent(index, this.velocityComponentType);

         assert velocityComponent != null;

         BoundingBox boundingBox = archetypeChunk.getComponent(index, this.boundingBoxComponentType);
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);

         try {
            if (projectileComponent.consumeDeadTimer(dt)) {
               projectileComponent.onProjectileDeath(ref, transformComponent.getPosition(), commandBuffer);
               commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
               return;
            }

            World world = commandBuffer.getExternalData().getWorld();
            projectileComponent.getSimplePhysicsProvider().tick(dt, velocityComponent, world, transformComponent, ref, commandBuffer);
         } catch (Throwable var12) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var12)).log("Exception while ticking entity! Removing!! %s", projectileComponent);
            commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
         }
      }
   }
}
