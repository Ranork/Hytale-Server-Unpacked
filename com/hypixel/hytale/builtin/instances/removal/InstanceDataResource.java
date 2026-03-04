package com.hypixel.hytale.builtin.instances.removal;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.time.Instant;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InstanceDataResource implements Resource<ChunkStore> {
   @Nonnull
   public static final BuilderCodec<InstanceDataResource> CODEC = BuilderCodec.builder(InstanceDataResource.class, InstanceDataResource::new)
      .append(new KeyedCodec<>("TimeoutTimer", Codec.INSTANT), (o, i) -> o.timeoutTimer = i, o -> o.timeoutTimer)
      .add()
      .append(new KeyedCodec<>("IdleTimeoutTimer", Codec.INSTANT), (o, i) -> o.idleTimeoutTimer = i, o -> o.idleTimeoutTimer)
      .add()
      .append(new KeyedCodec<>("HadPlayer", Codec.BOOLEAN), (o, i) -> o.hadPlayer = i, o -> o.hadPlayer)
      .add()
      .append(new KeyedCodec<>("WorldTimeoutTimer", Codec.INSTANT), (o, i) -> o.worldTimeoutTimer = i, o -> o.worldTimeoutTimer)
      .add()
      .build();
   private boolean isRemoving;
   private Instant timeoutTimer;
   private Instant idleTimeoutTimer;
   private boolean hadPlayer;
   @Nullable
   private Instant worldTimeoutTimer;

   @Nonnull
   public static ResourceType<ChunkStore, InstanceDataResource> getResourceType() {
      return InstancesPlugin.get().getInstanceDataResourceType();
   }

   public boolean isRemoving() {
      return this.isRemoving;
   }

   public void setRemoving(boolean removing) {
      this.isRemoving = removing;
   }

   public Instant getTimeoutTimer() {
      return this.timeoutTimer;
   }

   public void setTimeoutTimer(Instant timeoutTimer) {
      this.timeoutTimer = timeoutTimer;
   }

   public Instant getIdleTimeoutTimer() {
      return this.idleTimeoutTimer;
   }

   public void setIdleTimeoutTimer(Instant idleTimeoutTimer) {
      this.idleTimeoutTimer = idleTimeoutTimer;
   }

   public boolean hadPlayer() {
      return this.hadPlayer;
   }

   public void setHadPlayer(boolean hadPlayer) {
      this.hadPlayer = hadPlayer;
   }

   @Nullable
   public Instant getWorldTimeoutTimer() {
      return this.worldTimeoutTimer;
   }

   public void setWorldTimeoutTimer(@Nullable Instant worldTimeoutTimer) {
      this.worldTimeoutTimer = worldTimeoutTimer;
   }

   @Nonnull
   public InstanceDataResource clone() {
      return new InstanceDataResource();
   }
}
