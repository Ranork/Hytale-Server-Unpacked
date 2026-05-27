package com.hypixel.hytale.server.core.modules.entity.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.data.unknown.UnknownComponents;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentDisplayName;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DisplayNameSystems {
   public static class HydrateDisplayName extends HolderSystem<EntityStore> {
      @Nonnull
      private static final Query<EntityStore> QUERY = Query.and(PersistentDisplayName.getComponentType(), Query.not(DisplayNameComponent.getComponentType()));

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return QUERY;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         PersistentDisplayName persistentDisplayName = holder.getComponent(PersistentDisplayName.getComponentType());

         assert persistentDisplayName != null;

         holder.putComponent(DisplayNameComponent.getComponentType(), new DisplayNameComponent(persistentDisplayName.getDisplayName()));
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class MigratePlayerDisplayName extends EntityModule.MigrationSystem {
      @Nonnull
      private static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(new SystemDependency<>(Order.BEFORE, DisplayNameSystems.HydrateDisplayName.class));

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return EntityStore.REGISTRY.getUnknownComponentType();
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return DEPENDENCIES;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         UnknownComponents<EntityStore> unknownComponents = holder.getComponent(EntityStore.REGISTRY.getUnknownComponentType());

         assert unknownComponents != null;

         if (unknownComponents.contains("DisplayName")) {
            PersistentDisplayName decoded = unknownComponents.removeComponent("DisplayName", PersistentDisplayName.CODEC);
            if (holder.getComponent(Player.getComponentType()) == null) {
               if (decoded != null) {
                  holder.putComponent(PersistentDisplayName.getComponentType(), decoded);
               }
            }
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class SyncDisplayName extends RefChangeSystem<EntityStore, PersistentDisplayName> {
      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return PersistentDisplayName.getComponentType();
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, PersistentDisplayName> componentType() {
         return PersistentDisplayName.getComponentType();
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull PersistentDisplayName component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         commandBuffer.putComponent(ref, DisplayNameComponent.getComponentType(), new DisplayNameComponent(component.getDisplayName()));
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         @Nullable PersistentDisplayName oldComponent,
         @Nonnull PersistentDisplayName newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         commandBuffer.putComponent(ref, DisplayNameComponent.getComponentType(), new DisplayNameComponent(newComponent.getDisplayName()));
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull PersistentDisplayName component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
         Message displayName = playerRef != null ? Message.raw(playerRef.getUsername()) : null;
         commandBuffer.putComponent(ref, DisplayNameComponent.getComponentType(), new DisplayNameComponent(displayName));
      }
   }
}
