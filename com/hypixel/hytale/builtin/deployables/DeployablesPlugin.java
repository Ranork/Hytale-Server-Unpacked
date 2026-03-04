package com.hypixel.hytale.builtin.deployables;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.deployables.component.DeployableComponent;
import com.hypixel.hytale.builtin.deployables.component.DeployableOwnerComponent;
import com.hypixel.hytale.builtin.deployables.component.DeployableProjectileComponent;
import com.hypixel.hytale.builtin.deployables.component.DeployableProjectileShooterComponent;
import com.hypixel.hytale.builtin.deployables.config.DeployableAoeConfig;
import com.hypixel.hytale.builtin.deployables.config.DeployableConfig;
import com.hypixel.hytale.builtin.deployables.config.DeployableSpawner;
import com.hypixel.hytale.builtin.deployables.config.DeployableTrapConfig;
import com.hypixel.hytale.builtin.deployables.config.DeployableTrapSpawnerConfig;
import com.hypixel.hytale.builtin.deployables.config.DeployableTurretConfig;
import com.hypixel.hytale.builtin.deployables.interaction.SpawnDeployableAtHitLocationInteraction;
import com.hypixel.hytale.builtin.deployables.interaction.SpawnDeployableFromRaycastInteraction;
import com.hypixel.hytale.builtin.deployables.system.DeployablesSystem;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class DeployablesPlugin extends JavaPlugin {
   private static DeployablesPlugin instance;
   private ComponentType<EntityStore, DeployableComponent> deployableComponentType;
   private ComponentType<EntityStore, DeployableOwnerComponent> deployableOwnerComponentType;
   private ComponentType<EntityStore, DeployableProjectileShooterComponent> deployableProjectileShooterComponentType;
   private ComponentType<EntityStore, DeployableProjectileComponent> deployableProjectileComponentType;

   public DeployablesPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   public static DeployablesPlugin get() {
      return instance;
   }

   @Override
   protected void setup() {
      instance = this;
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              DeployableSpawner.class, new DefaultAssetMap()
                           )
                           .setPath("DeployableSpawners"))
                        .setCodec(DeployableSpawner.CODEC))
                     .setKeyFunction(DeployableSpawner::getId))
                  .loadsAfter(ModelAsset.class, EntityEffect.class, SoundEvent.class))
               .loadsBefore(Interaction.class))
            .build()
      );
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      this.deployableComponentType = entityStoreRegistry.registerComponent(DeployableComponent.class, DeployableComponent::new);
      this.deployableOwnerComponentType = entityStoreRegistry.registerComponent(DeployableOwnerComponent.class, DeployableOwnerComponent::new);
      this.deployableProjectileShooterComponentType = entityStoreRegistry.registerComponent(
         DeployableProjectileShooterComponent.class, DeployableProjectileShooterComponent::new
      );
      this.deployableProjectileComponentType = entityStoreRegistry.registerComponent(DeployableProjectileComponent.class, DeployableProjectileComponent::new);
      DeployableConfig.CODEC.register("Trap", DeployableTrapConfig.class, DeployableTrapConfig.CODEC);
      DeployableConfig.CODEC.register("TrapSpawner", DeployableTrapSpawnerConfig.class, DeployableTrapSpawnerConfig.CODEC);
      DeployableConfig.CODEC.register("Aoe", DeployableAoeConfig.class, DeployableAoeConfig.CODEC);
      DeployableConfig.CODEC.register("Turret", DeployableTurretConfig.class, DeployableTurretConfig.CODEC);
      Interaction.CODEC.register("SpawnDeployableAtHitLocation", SpawnDeployableAtHitLocationInteraction.class, SpawnDeployableAtHitLocationInteraction.CODEC);
      Interaction.CODEC.register("SpawnDeployableFromRaycast", SpawnDeployableFromRaycastInteraction.class, SpawnDeployableFromRaycastInteraction.CODEC);
      entityStoreRegistry.registerSystem(new DeployablesSystem.DeployableTicker());
      entityStoreRegistry.registerSystem(new DeployablesSystem.DeployableRegisterer());
      entityStoreRegistry.registerSystem(new DeployablesSystem.DeployableOwnerTicker());
   }

   public ComponentType<EntityStore, DeployableComponent> getDeployableComponentType() {
      return this.deployableComponentType;
   }

   public ComponentType<EntityStore, DeployableOwnerComponent> getDeployableOwnerComponentType() {
      return this.deployableOwnerComponentType;
   }

   public ComponentType<EntityStore, DeployableProjectileShooterComponent> getDeployableProjectileShooterComponentType() {
      return this.deployableProjectileShooterComponentType;
   }

   public ComponentType<EntityStore, DeployableProjectileComponent> getDeployableProjectileComponentType() {
      return this.deployableProjectileComponentType;
   }
}
