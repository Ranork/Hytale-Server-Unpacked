package com.hypixel.hytale.builtin.deployables.component;

import com.hypixel.hytale.builtin.deployables.DeployablesPlugin;
import com.hypixel.hytale.builtin.deployables.config.DeployableConfig;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DeployableComponent implements Component<EntityStore> {
   @Nonnull
   private final Map<DeployableComponent.DeployableFlag, Integer> flags = new EnumMap<>(DeployableComponent.DeployableFlag.class);
   private DeployableConfig config;
   private Ref<EntityStore> owner;
   private UUID ownerUUID;
   private Instant spawnInstant;
   private float timeSinceLastAttack;
   @Nullable
   private Vector3f debugColor;
   private boolean firstTickRan;
   private String spawnFace;

   @Nonnull
   public static ComponentType<EntityStore, DeployableComponent> getComponentType() {
      return DeployablesPlugin.get().getDeployableComponentType();
   }

   @Override
   public Component<EntityStore> clone() {
      return this;
   }

   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      if (!this.firstTickRan) {
         this.config.firstTick(this, dt, index, archetypeChunk, store, commandBuffer);
         this.firstTickRan = true;
      }

      this.config.tick(this, dt, index, archetypeChunk, store, commandBuffer);
   }

   public void init(
      @Nonnull Ref<EntityStore> deployerRef,
      @Nonnull Store<EntityStore> store,
      @Nonnull DeployableConfig config,
      @Nonnull Instant spawnInstant,
      @Nonnull String spawnFace
   ) {
      UUIDComponent uuidComponent = store.getComponent(deployerRef, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      this.config = config;
      this.owner = deployerRef;
      this.spawnInstant = spawnInstant;
      this.spawnFace = spawnFace;
      this.ownerUUID = uuidComponent.getUuid();
   }

   public Ref<EntityStore> getOwner() {
      return this.owner;
   }

   public UUID getOwnerUUID() {
      return this.ownerUUID;
   }

   public DeployableConfig getConfig() {
      return this.config;
   }

   public Instant getSpawnInstant() {
      return this.spawnInstant;
   }

   public float getTimeSinceLastAttack() {
      return this.timeSinceLastAttack;
   }

   public void setTimeSinceLastAttack(float time) {
      this.timeSinceLastAttack = time;
   }

   public float incrementTimeSinceLastAttack(float time) {
      return this.timeSinceLastAttack += time;
   }

   public String getSpawnFace() {
      return this.spawnFace;
   }

   public int getFlag(@Nonnull DeployableComponent.DeployableFlag key) {
      return this.flags.computeIfAbsent(key, k -> 0);
   }

   public void setFlag(@Nonnull DeployableComponent.DeployableFlag key, int value) {
      this.flags.put(key, value);
   }

   @Nonnull
   public Vector3f getDebugColor() {
      if (this.debugColor == null) {
         ThreadLocalRandom random = ThreadLocalRandom.current();
         this.debugColor = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
      }

      return this.debugColor;
   }

   public static enum DeployableFlag {
      STATE,
      LIVE,
      BURST_SHOTS,
      TRIGGERED;
   }
}
