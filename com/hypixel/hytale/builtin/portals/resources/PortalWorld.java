package com.hypixel.hytale.builtin.portals.resources;

import com.hypixel.hytale.builtin.portals.PortalsPlugin;
import com.hypixel.hytale.builtin.portals.components.voidevent.config.VoidEventConfig;
import com.hypixel.hytale.builtin.portals.integrations.PortalGameplayConfig;
import com.hypixel.hytale.builtin.portals.integrations.PortalRemovalCondition;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.packets.interface_.PortalDef;
import com.hypixel.hytale.protocol.packets.interface_.PortalState;
import com.hypixel.hytale.protocol.packets.interface_.UpdatePortal;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.portalworld.PortalType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PortalWorld implements Resource<EntityStore> {
   private String portalTypeId;
   private int timeLimitSeconds;
   private PortalRemovalCondition worldRemovalCondition;
   private PortalGameplayConfig storedGameplayConfig;
   private Set<UUID> diedInWorld;
   private Set<UUID> seesUi;
   private Transform spawnPoint;
   @Nullable
   private Ref<EntityStore> voidEventRef;

   public static ResourceType<EntityStore, PortalWorld> getResourceType() {
      return PortalsPlugin.getInstance().getPortalResourceType();
   }

   public void init(
      @Nonnull PortalType portalType, int timeLimitSeconds, @Nonnull PortalRemovalCondition removalCondition, @Nonnull PortalGameplayConfig gameplayConfig
   ) {
      this.portalTypeId = portalType.getId();
      this.timeLimitSeconds = timeLimitSeconds;
      this.worldRemovalCondition = removalCondition;
      this.storedGameplayConfig = gameplayConfig;
      this.diedInWorld = Collections.newSetFromMap(new ConcurrentHashMap<>());
      this.seesUi = Collections.newSetFromMap(new ConcurrentHashMap<>());
   }

   @Nullable
   public PortalType getPortalType() {
      return this.portalTypeId == null ? null : PortalType.getAssetMap().getAsset(this.portalTypeId);
   }

   public boolean exists() {
      return this.getPortalType() != null;
   }

   public int getTimeLimitSeconds() {
      return this.timeLimitSeconds;
   }

   public double getElapsedSeconds(@Nonnull World world) {
      return this.worldRemovalCondition.getElapsedSeconds(world);
   }

   public double getRemainingSeconds(@Nonnull World world) {
      return this.worldRemovalCondition.getRemainingSeconds(world);
   }

   public static void setRemainingSeconds(@Nonnull World world, double seconds) {
      PortalRemovalCondition.setRemainingSeconds(world, seconds);
   }

   public Set<UUID> getDiedInWorld() {
      return this.diedInWorld;
   }

   public Set<UUID> getSeesUi() {
      return this.seesUi;
   }

   public PortalGameplayConfig getGameplayConfig() {
      PortalType portalType = this.getPortalType();
      if (portalType == null) {
         return this.storedGameplayConfig;
      } else {
         GameplayConfig gameplayConfig = this.getPortalType().getGameplayConfig();
         PortalGameplayConfig portalGameplayConfig = gameplayConfig == null ? null : gameplayConfig.getPluginConfig().get(PortalGameplayConfig.class);
         return portalGameplayConfig != null ? portalGameplayConfig : this.storedGameplayConfig;
      }
   }

   @Nullable
   public VoidEventConfig getVoidEventConfig() {
      return this.getGameplayConfig().getVoidEvent();
   }

   @Nullable
   public Transform getSpawnPoint() {
      return this.spawnPoint;
   }

   public void setSpawnPoint(Transform spawnPoint) {
      this.spawnPoint = spawnPoint;
   }

   @Nullable
   public Ref<EntityStore> getVoidEventRef() {
      if (this.voidEventRef != null && !this.voidEventRef.isValid()) {
         this.voidEventRef = null;
      }

      return this.voidEventRef;
   }

   public boolean isVoidEventActive() {
      return this.getVoidEventRef() != null;
   }

   public void setVoidEventRef(@Nullable Ref<EntityStore> voidEventRef) {
      this.voidEventRef = voidEventRef;
   }

   @Nonnull
   public UpdatePortal createFullPacket(@Nonnull World world) {
      PortalType portalType = this.getPortalType();
      boolean hasBreach = portalType.isVoidInvasionEnabled();
      int explorationSeconds;
      int breachSeconds;
      if (hasBreach) {
         VoidEventConfig voidEvent = this.getGameplayConfig().getVoidEvent();
         breachSeconds = voidEvent.getDurationSeconds();
         explorationSeconds = this.timeLimitSeconds - breachSeconds;
      } else {
         explorationSeconds = this.timeLimitSeconds;
         breachSeconds = 0;
      }

      PortalDef portalDef = new PortalDef(portalType.getDescription().getDisplayNameKey(), explorationSeconds, breachSeconds);
      return new UpdatePortal(this.createStateForPacket(world), portalDef);
   }

   @Nonnull
   public UpdatePortal createUpdatePacket(@Nonnull World world) {
      return new UpdatePortal(this.createStateForPacket(world), null);
   }

   @Nonnull
   private PortalState createStateForPacket(@Nonnull World world) {
      double remainingSeconds = this.worldRemovalCondition.getRemainingSeconds(world);
      VoidEventConfig voidEvent = this.getVoidEventConfig();
      PortalType portalType = this.getPortalType();
      if (voidEvent != null && portalType != null) {
         int breachSeconds = voidEvent.getDurationSeconds();
         if (portalType.isVoidInvasionEnabled() && remainingSeconds > breachSeconds) {
            remainingSeconds -= breachSeconds;
         }
      }

      return new PortalState((int)Math.ceil(remainingSeconds), this.isVoidEventActive());
   }

   @Override
   public Resource<EntityStore> clone() {
      PortalWorld clone = new PortalWorld();
      clone.portalTypeId = this.portalTypeId;
      clone.timeLimitSeconds = this.timeLimitSeconds;
      clone.worldRemovalCondition = this.worldRemovalCondition;
      return clone;
   }
}
