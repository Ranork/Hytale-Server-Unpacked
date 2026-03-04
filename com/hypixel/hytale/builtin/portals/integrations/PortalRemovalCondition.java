package com.hypixel.hytale.builtin.portals.integrations;

import com.hypixel.hytale.builtin.instances.removal.InstanceDataResource;
import com.hypixel.hytale.builtin.instances.removal.RemovalCondition;
import com.hypixel.hytale.builtin.instances.removal.TimeoutCondition;
import com.hypixel.hytale.builtin.instances.removal.WorldEmptyCondition;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import java.time.Instant;
import javax.annotation.Nonnull;

public class PortalRemovalCondition implements RemovalCondition {
   @Nonnull
   public static final BuilderCodec<PortalRemovalCondition> CODEC = BuilderCodec.builder(PortalRemovalCondition.class, PortalRemovalCondition::new)
      .documentation("A condition for temporary portal worlds.")
      .<Double>append(new KeyedCodec<>("TimeoutSeconds", Codec.DOUBLE), PortalRemovalCondition::setTimeLimitSeconds, o -> o.getTimeLimitSeconds())
      .documentation("How long the portal world will stay open (in seconds) after being joined.")
      .add()
      .build();
   @Nonnull
   private final WorldEmptyCondition worldEmptyCondition = new WorldEmptyCondition(90.0);
   private TimeoutCondition timeLimitCondition;

   public PortalRemovalCondition() {
      this(60.0);
   }

   public PortalRemovalCondition(double timeLimitSeconds) {
      this.timeLimitCondition = new TimeoutCondition(timeLimitSeconds);
   }

   private double getTimeLimitSeconds() {
      return this.timeLimitCondition.getTimeoutSeconds();
   }

   private void setTimeLimitSeconds(double timeLimitSeconds) {
      this.timeLimitCondition = new TimeoutCondition(timeLimitSeconds);
   }

   public double getElapsedSeconds(@Nonnull World world) {
      double timeLimitSeconds = this.timeLimitCondition.getTimeoutSeconds();
      double remainingSeconds = this.getRemainingSeconds(world);
      return Math.max(0.0, timeLimitSeconds - remainingSeconds);
   }

   public double getRemainingSeconds(@Nonnull World world) {
      Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
      Store<EntityStore> entityStore = world.getEntityStore().getStore();
      InstanceDataResource instanceData = chunkStore.getResource(InstanceDataResource.getResourceType());
      TimeResource timeResource = entityStore.getResource(TimeResource.getResourceType());
      Instant timeoutInstant = instanceData.getTimeoutTimer();
      if (timeoutInstant == null) {
         return this.timeLimitCondition.getTimeoutSeconds();
      } else {
         double remainingSeconds = Duration.between(timeResource.getNow(), timeoutInstant).toNanos() / 1.0E9;
         return Math.max(0.0, remainingSeconds);
      }
   }

   public static void setRemainingSeconds(@Nonnull World world, double seconds) {
      seconds = Math.max(0.0, seconds);
      Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
      Store<EntityStore> entityStore = world.getEntityStore().getStore();
      InstanceDataResource instanceData = chunkStore.getResource(InstanceDataResource.getResourceType());
      TimeResource timeResource = entityStore.getResource(TimeResource.getResourceType());
      instanceData.setTimeoutTimer(timeResource.getNow().plusMillis((long)(seconds * 1000.0)));
   }

   @Override
   public boolean shouldRemoveWorld(@Nonnull Store<ChunkStore> store) {
      InstanceDataResource instanceData = store.getResource(InstanceDataResource.getResourceType());
      return instanceData.hadPlayer() && this.timeLimitCondition.shouldRemoveWorld(store) ? true : this.worldEmptyCondition.shouldRemoveWorld(store);
   }
}
