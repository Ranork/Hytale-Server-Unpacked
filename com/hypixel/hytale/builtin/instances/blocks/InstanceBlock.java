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
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InstanceBlock implements Component<ChunkStore> {
   @Nonnull
   public static final BuilderCodec<InstanceBlock> CODEC = BuilderCodec.builder(InstanceBlock.class, InstanceBlock::new)
      .appendInherited(new KeyedCodec<>("WorldName", Codec.UUID_BINARY), (o, i) -> o.worldUUID = i, o -> o.worldUUID, (o, p) -> o.worldUUID = p.worldUUID)
      .add()
      .appendInherited(
         new KeyedCodec<>("CloseOnBlockRemove", Codec.BOOLEAN),
         (o, i) -> o.closeOnRemove = i,
         o -> o.closeOnRemove,
         (o, p) -> o.closeOnRemove = p.closeOnRemove
      )
      .add()
      .build();
   protected UUID worldUUID;
   protected CompletableFuture<World> worldFuture;
   protected boolean closeOnRemove = true;

   @Nonnull
   public static ComponentType<ChunkStore, InstanceBlock> getComponentType() {
      return InstancesPlugin.get().getInstanceBlockComponentType();
   }

   public InstanceBlock() {
   }

   public InstanceBlock(UUID worldUUID, boolean closeOnRemove) {
      this.worldUUID = worldUUID;
      this.closeOnRemove = closeOnRemove;
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

   @Nullable
   @Override
   public Component<ChunkStore> clone() {
      return new InstanceBlock(this.worldUUID, this.closeOnRemove);
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
         InstanceBlock instance = commandBuffer.getComponent(ref, InstanceBlock.getComponentType());

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
         return InstanceBlock.getComponentType();
      }
   }
}
