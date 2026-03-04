package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.components.messaging.MessageSupport;
import com.hypixel.hytale.server.npc.components.messaging.NPCBlockEventSupport;
import com.hypixel.hytale.server.npc.components.messaging.NPCEntityEventSupport;
import com.hypixel.hytale.server.npc.components.messaging.NPCMessage;
import com.hypixel.hytale.server.npc.components.messaging.PlayerBlockEventSupport;
import com.hypixel.hytale.server.npc.components.messaging.PlayerEntityEventSupport;
import java.util.Set;
import javax.annotation.Nonnull;

public abstract class MessageSupportSystem<T extends MessageSupport> extends SteppableTickingSystem {
   @Nonnull
   private final ComponentType<EntityStore, T> messageSupportComponentType;
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies;

   public MessageSupportSystem(@Nonnull ComponentType<EntityStore, T> messageSupportComponentType, @Nonnull Set<Dependency<EntityStore>> dependencies) {
      this.messageSupportComponentType = messageSupportComponentType;
      this.dependencies = dependencies;
   }

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   @Override
   public boolean isParallel(int archetypeChunkSize, int taskCount) {
      return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
   }

   @Override
   public void steppedTick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      T messageSupportComponent = archetypeChunk.getComponent(index, this.messageSupportComponentType);

      assert messageSupportComponent != null;

      NPCMessage[] messageSlots = messageSupportComponent.getMessageSlots();
      if (messageSlots != null) {
         for (NPCMessage slot : messageSlots) {
            if (slot.isActivated() && !slot.isInfinite() && slot.tickAge(dt)) {
               slot.deactivate();
            }
         }
      }
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.messageSupportComponentType;
   }

   public static class BeaconSystem extends MessageSupportSystem<BeaconSupport> {
      public BeaconSystem(@Nonnull ComponentType<EntityStore, BeaconSupport> componentType, @Nonnull Set<Dependency<EntityStore>> dependencies) {
         super(componentType, dependencies);
      }
   }

   public static class NPCBlockEventSystem extends MessageSupportSystem<NPCBlockEventSupport> {
      public NPCBlockEventSystem(@Nonnull ComponentType<EntityStore, NPCBlockEventSupport> componentType, @Nonnull Set<Dependency<EntityStore>> dependencies) {
         super(componentType, dependencies);
      }
   }

   public static class NPCEntityEventSystem extends MessageSupportSystem<NPCEntityEventSupport> {
      public NPCEntityEventSystem(@Nonnull ComponentType<EntityStore, NPCEntityEventSupport> componentType, @Nonnull Set<Dependency<EntityStore>> dependencies) {
         super(componentType, dependencies);
      }
   }

   public static class PlayerBlockEventSystem extends MessageSupportSystem<PlayerBlockEventSupport> {
      public PlayerBlockEventSystem(
         @Nonnull ComponentType<EntityStore, PlayerBlockEventSupport> componentType, @Nonnull Set<Dependency<EntityStore>> dependencies
      ) {
         super(componentType, dependencies);
      }
   }

   public static class PlayerEntityEventSystem extends MessageSupportSystem<PlayerEntityEventSupport> {
      public PlayerEntityEventSystem(
         @Nonnull ComponentType<EntityStore, PlayerEntityEventSupport> componentType, @Nonnull Set<Dependency<EntityStore>> dependencies
      ) {
         super(componentType, dependencies);
      }
   }
}
