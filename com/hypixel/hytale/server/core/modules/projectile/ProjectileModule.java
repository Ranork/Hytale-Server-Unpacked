package com.hypixel.hytale.server.core.modules.projectile;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.AudioComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.modules.projectile.component.PredictedProjectile;
import com.hypixel.hytale.server.core.modules.projectile.component.Projectile;
import com.hypixel.hytale.server.core.modules.projectile.config.PhysicsConfig;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsConfig;
import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsProvider;
import com.hypixel.hytale.server.core.modules.projectile.interaction.ProjectileInteraction;
import com.hypixel.hytale.server.core.modules.projectile.system.PredictedProjectileSystems;
import com.hypixel.hytale.server.core.modules.projectile.system.StandardPhysicsTickSystem;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ProjectileModule extends JavaPlugin {
   @Nonnull
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(ProjectileModule.class)
      .description(
         "This module implements the new projectile system. Disabling this module will prevent anything using the new projectile system from functioning."
      )
      .depends(CollisionModule.class)
      .depends(EntityModule.class)
      .build();
   private static ProjectileModule instance;
   private ComponentType<EntityStore, Projectile> projectileComponentType;
   private ComponentType<EntityStore, StandardPhysicsProvider> standardPhysicsProviderComponentType;
   private ComponentType<EntityStore, PredictedProjectile> predictedProjectileComponentType;

   public static ProjectileModule get() {
      return instance;
   }

   public ProjectileModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      this.getCodecRegistry(Interaction.CODEC).register("Projectile", ProjectileInteraction.class, ProjectileInteraction.CODEC);
      this.projectileComponentType = entityStoreRegistry.registerComponent(Projectile.class, "IsProjectile", Projectile.CODEC);
      this.predictedProjectileComponentType = entityStoreRegistry.registerComponent(PredictedProjectile.class, () -> {
         throw new UnsupportedOperationException();
      });
      this.standardPhysicsProviderComponentType = entityStoreRegistry.registerComponent(StandardPhysicsProvider.class, () -> {
         throw new UnsupportedOperationException();
      });
      entityStoreRegistry.registerSystem(new StandardPhysicsTickSystem());
      entityStoreRegistry.registerSystem(new PredictedProjectileSystems.EntityTrackerUpdate());
      this.getCodecRegistry(PhysicsConfig.CODEC).register("Standard", StandardPhysicsConfig.class, StandardPhysicsConfig.CODEC);
   }

   @Nonnull
   public Ref<EntityStore> spawnProjectile(
      Ref<EntityStore> creatorRef,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull ProjectileConfig config,
      @Nonnull Vector3d position,
      @Nonnull Vector3d direction
   ) {
      return this.spawnProjectile(null, creatorRef, commandBuffer, config, position, direction);
   }

   @Nonnull
   public Ref<EntityStore> spawnProjectile(
      @Nullable UUID predictionId,
      Ref<EntityStore> creatorRef,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull ProjectileConfig config,
      @Nonnull Vector3d position,
      @Nonnull Vector3d direction
   ) {
      Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
      Vector3f rotation = new Vector3f();
      Direction rotationOffset = config.getSpawnRotationOffset();
      rotation.setYaw(PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(direction.x, direction.z)));
      rotation.setPitch(PhysicsMath.pitchFromDirection(direction.x, direction.y, direction.z));
      rotation.add(rotationOffset.pitch, rotationOffset.yaw, rotationOffset.roll);
      PhysicsMath.vectorFromAngles(rotation.getYaw(), rotation.getPitch(), direction);
      Vector3d offset = config.getCalculatedOffset(rotation.getPitch(), rotation.getYaw());
      position.add(offset);
      holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(position, rotation));
      holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
      if (predictionId != null) {
         holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(predictionId));
      }

      holder.addComponent(Interactions.getComponentType(), new Interactions(config.getInteractions()));
      Model model = config.getModel();
      holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
      holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
      holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
      holder.addComponent(NetworkId.getComponentType(), new NetworkId(commandBuffer.getExternalData().takeNextNetworkId()));
      holder.ensureComponent(Projectile.getComponentType());
      if (predictionId != null) {
         holder.addComponent(PredictedProjectile.getComponentType(), new PredictedProjectile(predictionId));
      }

      holder.addComponent(Velocity.getComponentType(), new Velocity());
      config.getPhysicsConfig().apply(holder, creatorRef, direction.clone().scale(config.getLaunchForce()), commandBuffer, predictionId != null);
      holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());
      holder.addComponent(
         DespawnComponent.getComponentType(),
         new DespawnComponent(commandBuffer.getResource(TimeResource.getResourceType()).getNow().plus(Duration.ofSeconds(300L)))
      );
      int launchWorldSoundEventIndex = config.getLaunchWorldSoundEventIndex();
      if (launchWorldSoundEventIndex != 0) {
         SoundUtil.playSoundEvent3d(
            launchWorldSoundEventIndex, SoundCategory.SFX, position.x, position.y, position.z, targetRef -> !targetRef.equals(creatorRef), commandBuffer
         );
      }

      int projectileSoundEventIndex = config.getProjectileSoundEventIndex();
      if (projectileSoundEventIndex != 0) {
         AudioComponent audioComponent = new AudioComponent();
         audioComponent.addSound(projectileSoundEventIndex);
         holder.addComponent(AudioComponent.getComponentType(), audioComponent);
      }

      Ref<EntityStore> projectileRef = commandBuffer.addEntity(holder, AddReason.SPAWN);
      if (predictionId == null && creatorRef != null) {
         commandBuffer.run(store -> onProjectileSpawnInteraction(projectileRef, creatorRef, store));
      }

      return projectileRef;
   }

   private static void onProjectileSpawnInteraction(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> creatorRef, @Nonnull Store<EntityStore> store) {
      InteractionManager interactionManagerComponent = store.getComponent(creatorRef, InteractionModule.get().getInteractionManagerComponent());
      if (interactionManagerComponent != null) {
         if (EntityUtils.getEntity(creatorRef, store) instanceof LivingEntity livingEntity) {
            InteractionContext context = InteractionContext.forProxyEntity(interactionManagerComponent, livingEntity, ref);
            String rootInteractionId = context.getRootInteractionId(InteractionType.ProjectileSpawn);
            if (rootInteractionId != null) {
               RootInteraction rootInteraction = RootInteraction.getRootInteractionOrUnknown(rootInteractionId);
               if (rootInteraction != null) {
                  InteractionChain chain = interactionManagerComponent.initChain(InteractionType.ProjectileSpawn, context, rootInteraction, true);
                  interactionManagerComponent.queueExecuteChain(chain);
               }
            }
         }
      }
   }

   @Nonnull
   public ComponentType<EntityStore, Projectile> getProjectileComponentType() {
      return this.projectileComponentType;
   }

   @Nonnull
   public ComponentType<EntityStore, StandardPhysicsProvider> getStandardPhysicsProviderComponentType() {
      return this.standardPhysicsProviderComponentType;
   }

   @Nonnull
   public ComponentType<EntityStore, PredictedProjectile> getPredictedProjectileComponentType() {
      return this.predictedProjectileComponentType;
   }
}
