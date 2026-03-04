package com.hypixel.hytale.builtin.portals.systems;

import com.hypixel.hytale.builtin.portals.components.PortalDevice;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CloseWorldWhenBreakingDeviceSystems {
   private CloseWorldWhenBreakingDeviceSystems() {
   }

   private static void maybeCloseFragmentWorld(@Nullable PortalDevice device) {
      if (device != null) {
         World world = device.getDestinationWorld();
         if (world != null && world.getPlayerCount() <= 0) {
            Universe.get().removeWorld(world.getName());
         }
      }
   }

   public static class ComponentRemoved extends RefChangeSystem<ChunkStore, PortalDevice> {
      @Nonnull
      @Override
      public ComponentType<ChunkStore, PortalDevice> componentType() {
         return PortalDevice.getComponentType();
      }

      public void onComponentAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull PortalDevice component, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
      }

      public void onComponentSet(
         @Nonnull Ref<ChunkStore> ref,
         @Nullable PortalDevice oldComponent,
         @Nonnull PortalDevice newComponent,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<ChunkStore> ref, @Nonnull PortalDevice component, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         CloseWorldWhenBreakingDeviceSystems.maybeCloseFragmentWorld(component);
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.componentType();
      }
   }

   public static class EntityRemoved extends RefSystem<ChunkStore> {
      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         PortalDevice device = store.getComponent(ref, PortalDevice.getComponentType());
         CloseWorldWhenBreakingDeviceSystems.maybeCloseFragmentWorld(device);
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return PortalDevice.getComponentType();
      }
   }
}
