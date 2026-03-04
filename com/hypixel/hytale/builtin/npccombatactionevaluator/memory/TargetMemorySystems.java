package com.hypixel.hytale.builtin.npccombatactionevaluator.memory;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.systems.RoleSystems;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class TargetMemorySystems {
   @Nonnull
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public static class Ticking extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private static final String HOSTILE = "hostile";
      @Nonnull
      private static final String FRIENDLY = "friendly";
      @Nonnull
      private final ComponentType<EntityStore, TargetMemory> targetMemoryComponentType;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;

      public Ticking(@Nonnull ComponentType<EntityStore, TargetMemory> targetMemoryComponentType) {
         this.targetMemoryComponentType = targetMemoryComponentType;
         this.dependencies = Set.of(new SystemDependency<>(Order.BEFORE, RoleSystems.BehaviourTickSystem.class));
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.targetMemoryComponentType;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         TargetMemory targetMemoryComponent = archetypeChunk.getComponent(index, this.targetMemoryComponentType);

         assert targetMemoryComponent != null;

         Int2FloatOpenHashMap hostileMap = targetMemoryComponent.getKnownHostiles();
         List<Ref<EntityStore>> hostileList = targetMemoryComponent.getKnownHostilesList();
         iterateMemory(dt, index, archetypeChunk, commandBuffer, hostileList, hostileMap, "hostile");
         Int2FloatOpenHashMap friendlyMap = targetMemoryComponent.getKnownFriendlies();
         List<Ref<EntityStore>> friendlyList = targetMemoryComponent.getKnownFriendliesList();
         iterateMemory(dt, index, archetypeChunk, commandBuffer, friendlyList, friendlyMap, "friendly");
         Ref<EntityStore> closestHostileRef = targetMemoryComponent.getClosestHostile();
         if (closestHostileRef != null && !isValidTarget(closestHostileRef, commandBuffer)) {
            targetMemoryComponent.setClosestHostile(null);
         }
      }

      private static void iterateMemory(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull List<Ref<EntityStore>> targetsList,
         @Nonnull Int2FloatOpenHashMap targetsMap,
         @Nonnull String type
      ) {
         for (int i = targetsList.size() - 1; i >= 0; i--) {
            Ref<EntityStore> ref = targetsList.get(i);
            if (!isValidTarget(ref, commandBuffer)) {
               removeEntry(index, archetypeChunk, i, ref, targetsList, targetsMap, type);
            } else {
               float timeRemaining = targetsMap.mergeFloat(ref.getIndex(), -dt, Float::sum);
               if (timeRemaining <= 0.0F) {
                  removeEntry(index, archetypeChunk, i, ref, targetsList, targetsMap, type);
               }
            }
         }
      }

      private static boolean isValidTarget(@Nonnull Ref<EntityStore> targetRef, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         if (!targetRef.isValid()) {
            return false;
         } else if (commandBuffer.getArchetype(targetRef).contains(DeathComponent.getComponentType())) {
            return false;
         } else {
            Player targetPlayerComponent = commandBuffer.getComponent(targetRef, Player.getComponentType());
            return targetPlayerComponent == null || targetPlayerComponent.getGameMode() == GameMode.Adventure;
         }
      }

      private static void removeEntry(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         int targetIndex,
         @Nonnull Ref<EntityStore> targetRef,
         @Nonnull List<Ref<EntityStore>> targetsList,
         @Nonnull Int2FloatOpenHashMap targetsMap,
         @Nonnull String type
      ) {
         targetsMap.remove(targetRef.getIndex());
         targetsList.remove(targetIndex);
         HytaleLogger.Api context = TargetMemorySystems.LOGGER.at(Level.FINEST);
         if (context.isEnabled()) {
            context.log("%s: Removed lost %s target %s", archetypeChunk.getReferenceTo(index), type, targetRef);
         }
      }
   }
}
