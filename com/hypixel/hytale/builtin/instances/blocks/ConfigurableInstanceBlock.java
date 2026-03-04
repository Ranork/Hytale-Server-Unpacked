package com.hypixel.hytale.builtin.instances.blocks;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConfigurableInstanceBlock implements Component<ChunkStore> {
   @Nonnull
   public static final BuilderCodec<ConfigurableInstanceBlock> CODEC = BuilderCodec.builder(ConfigurableInstanceBlock.class, ConfigurableInstanceBlock::new)
      .appendInherited(new KeyedCodec<>("WorldName", Codec.UUID_BINARY), (o, i) -> o.worldUUID = i, o -> o.worldUUID, (o, p) -> o.worldUUID = p.worldUUID)
      .add()
      .appendInherited(
         new KeyedCodec<>("CloseOnBlockRemove", Codec.BOOLEAN),
         (o, i) -> o.closeOnRemove = i,
         o -> o.closeOnRemove,
         (o, p) -> o.closeOnRemove = p.closeOnRemove
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("InstanceName", Codec.STRING), (o, i) -> o.instanceName = i, o -> o.instanceName, (o, p) -> o.instanceName = p.instanceName
      )
      .add()
      .appendInherited(new KeyedCodec<>("InstanceKey", Codec.STRING), (o, i) -> o.instanceKey = i, o -> o.instanceKey, (o, p) -> o.instanceKey = p.instanceKey)
      .add()
      .appendInherited(
         new KeyedCodec<>("PositionOffset", Vector3d.CODEC),
         (o, i) -> o.positionOffset = i,
         o -> o.positionOffset,
         (o, p) -> o.positionOffset = p.positionOffset
      )
      .add()
      .appendInherited(new KeyedCodec<>("Rotation", Vector3f.ROTATION), (o, i) -> o.rotation = i, o -> o.rotation, (o, p) -> o.rotation = p.rotation)
      .add()
      .appendInherited(
         new KeyedCodec<>("PersonalReturnPoint", Codec.BOOLEAN),
         (o, i) -> o.personalReturnPoint = i,
         o -> o.personalReturnPoint,
         (o, p) -> o.personalReturnPoint = p.personalReturnPoint
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("RemoveBlockAfter", Codec.DOUBLE),
         (o, i) -> o.removeBlockAfter = i,
         o -> o.removeBlockAfter,
         (o, p) -> o.removeBlockAfter = p.removeBlockAfter
      )
      .add()
      .build();
   protected UUID worldUUID;
   protected CompletableFuture<World> worldFuture;
   protected boolean closeOnRemove = true;
   private String instanceName;
   private String instanceKey;
   @Nullable
   private Vector3d positionOffset;
   @Nullable
   private Vector3f rotation;
   private boolean personalReturnPoint = false;
   private double removeBlockAfter = -1.0;

   @Nonnull
   public static ComponentType<ChunkStore, ConfigurableInstanceBlock> getComponentType() {
      return InstancesPlugin.get().getConfigurableInstanceBlockComponentType();
   }

   public ConfigurableInstanceBlock() {
   }

   public ConfigurableInstanceBlock(
      UUID worldUUID,
      boolean closeOnRemove,
      String instanceName,
      String instanceKey,
      @Nullable Vector3d positionOffset,
      @Nullable Vector3f rotation,
      boolean personalReturnPoint,
      double removeBlockAfter
   ) {
      this.worldUUID = worldUUID;
      this.closeOnRemove = closeOnRemove;
      this.instanceName = instanceName;
      this.instanceKey = instanceKey;
      this.positionOffset = positionOffset;
      this.rotation = rotation;
      this.personalReturnPoint = personalReturnPoint;
      this.removeBlockAfter = removeBlockAfter;
   }

   public UUID getWorldUUID() {
      return this.worldUUID;
   }

   public void setWorldUUID(UUID worldUUID) {
      this.worldUUID = worldUUID;
   }

   public CompletableFuture<World> getWorldFuture() {
      return this.worldFuture;
   }

   public void setWorldFuture(CompletableFuture<World> worldFuture) {
      this.worldFuture = worldFuture;
   }

   public boolean isCloseOnRemove() {
      return this.closeOnRemove;
   }

   public void setCloseOnRemove(boolean closeOnRemove) {
      this.closeOnRemove = closeOnRemove;
   }

   public String getInstanceName() {
      return this.instanceName;
   }

   public void setInstanceName(@Nonnull String instanceName) {
      this.instanceName = instanceName;
   }

   public String getInstanceKey() {
      return this.instanceKey;
   }

   public void setInstanceKey(@Nonnull String instanceKey) {
      this.instanceKey = instanceKey;
   }

   @Nullable
   public Vector3d getPositionOffset() {
      return this.positionOffset;
   }

   public void setPositionOffset(@Nullable Vector3d positionOffset) {
      this.positionOffset = positionOffset;
   }

   @Nullable
   public Vector3f getRotation() {
      return this.rotation;
   }

   public void setRotation(@Nullable Vector3f rotation) {
      this.rotation = rotation;
   }

   public boolean isPersonalReturnPoint() {
      return this.personalReturnPoint;
   }

   public void setPersonalReturnPoint(boolean personalReturnPoint) {
      this.personalReturnPoint = personalReturnPoint;
   }

   public double getRemoveBlockAfter() {
      return this.removeBlockAfter;
   }

   public void setRemoveBlockAfter(double removeBlockAfter) {
      this.removeBlockAfter = removeBlockAfter;
   }

   @Nullable
   @Override
   public Component<ChunkStore> clone() {
      return new ConfigurableInstanceBlock(
         this.worldUUID,
         this.closeOnRemove,
         this.instanceName,
         this.instanceKey,
         this.positionOffset,
         this.rotation,
         this.personalReturnPoint,
         this.removeBlockAfter
      );
   }

   public static class OnRemove extends RefSystem<ChunkStore> {
      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         ConfigurableInstanceBlock instance = commandBuffer.getComponent(ref, ConfigurableInstanceBlock.getComponentType());

         assert instance != null;

         if (instance.closeOnRemove) {
            if (instance.worldUUID != null) {
               InstancesPlugin.get();
               InstancesPlugin.safeRemoveInstance(instance.worldUUID);
            } else if (instance.worldFuture != null) {
               instance.worldFuture.thenAccept(world -> {
                  InstancesPlugin.get();
                  InstancesPlugin.safeRemoveInstance(world.getName());
               });
            }
         }
      }

      @Nullable
      @Override
      public Query<ChunkStore> getQuery() {
         return ConfigurableInstanceBlock.getComponentType();
      }
   }
}
