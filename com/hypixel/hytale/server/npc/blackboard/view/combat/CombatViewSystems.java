package com.hypixel.hytale.server.npc.blackboard.view.combat;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChargingInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.role.support.CombatSupport;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public class CombatViewSystems {
   private static void clearCombatData(@Nonnull CombatViewSystems.CombatData combatData, @Nonnull CombatViewSystems.CombatDataPool dataPool) {
      if (combatData.interpreted) {
         List<InterpretedCombatData> dataList = combatData.combatData;

         for (int i = 0; i < dataList.size(); i++) {
            dataPool.releaseCombatData(dataList.get(i));
         }

         dataList.clear();
         combatData.interpreted = false;
      }
   }

   @Nonnull
   public static List<InterpretedCombatData> getCombatData(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      CombatViewSystems.CombatData combatDataComponent = store.getComponent(ref, CombatViewSystems.CombatData.getComponentType());
      if (combatDataComponent.interpreted) {
         return combatDataComponent.unmodifiableCombatData;
      } else {
         InteractionManager interactionManager = store.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
         CombatViewSystems.CombatDataPool combatDataPool = store.getResource(CombatViewSystems.CombatDataPool.getResourceType());
         List<InterpretedCombatData> dataList = combatDataComponent.combatData;
         IndexedLookupTableAssetMap<String, RootInteraction> interactionAssetMap = RootInteraction.getAssetMap();
         Set<String> attackInteractions = interactionAssetMap.getKeysForTag(CombatSupport.ATTACK_TAG_INDEX);
         Set<String> meleeInteractions = interactionAssetMap.getKeysForTag(CombatSupport.MELEE_TAG_INDEX);
         Set<String> rangedInteractions = interactionAssetMap.getKeysForTag(CombatSupport.RANGED_TAG_INDEX);
         Set<String> blockInteractions = interactionAssetMap.getKeysForTag(CombatSupport.BLOCK_TAG_INDEX);
         interactionManager.forEachInteraction((chain, interaction, list) -> {
            String rootId = chain.getRootInteraction().getId();
            if (!attackInteractions.contains(rootId)) {
               return list;
            } else {
               InterpretedCombatData entry = combatDataPool.getEmptyCombatData();
               entry.setAttack(rootId);
               entry.setCurrentElapsedTime(chain.getTimeInSeconds());
               entry.setCharging(interaction instanceof ChargingInteraction);
               entry.setPerformingMeleeAttack(meleeInteractions.contains(rootId));
               entry.setPerformingRangedAttack(rangedInteractions.contains(rootId));
               entry.setPerformingBlock(blockInteractions.contains(rootId));
               list.add(entry);
               return list;
            }
         }, dataList);
         combatDataComponent.interpreted = true;
         return combatDataComponent.unmodifiableCombatData;
      }
   }

   public static class CombatData implements Component<EntityStore> {
      private final List<InterpretedCombatData> combatData = new ObjectArrayList();
      private final List<InterpretedCombatData> unmodifiableCombatData = Collections.unmodifiableList(this.combatData);
      private boolean interpreted;

      public static ComponentType<EntityStore, CombatViewSystems.CombatData> getComponentType() {
         return NPCPlugin.get().getCombatDataComponentType();
      }

      @Nonnull
      @Override
      public Component<EntityStore> clone() {
         CombatViewSystems.CombatData data = new CombatViewSystems.CombatData();
         data.interpreted = this.interpreted;

         for (int i = 0; i < this.combatData.size(); i++) {
            data.combatData.add(this.combatData.get(i).clone());
         }

         return data;
      }
   }

   public static class CombatDataPool implements Resource<EntityStore> {
      private final ArrayDeque<InterpretedCombatData> combatDataPool = new ArrayDeque<>();

      public static ResourceType<EntityStore, CombatViewSystems.CombatDataPool> getResourceType() {
         return NPCPlugin.get().getCombatDataPoolResourceType();
      }

      @Nonnull
      @Override
      public Resource<EntityStore> clone() {
         return new CombatViewSystems.CombatDataPool();
      }

      public InterpretedCombatData getEmptyCombatData() {
         return this.combatDataPool.isEmpty() ? new InterpretedCombatData() : this.combatDataPool.poll();
      }

      public void releaseCombatData(@Nonnull InterpretedCombatData combatData) {
         this.combatDataPool.push(combatData);
      }
   }

   public static class Ensure extends HolderSystem<EntityStore> {
      private final ComponentType<EntityStore, CombatViewSystems.CombatData> combatDataComponentType;

      public Ensure(ComponentType<EntityStore, CombatViewSystems.CombatData> combatDataComponentType) {
         this.combatDataComponentType = combatDataComponentType;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return AllLegacyEntityTypesQuery.INSTANCE;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(this.combatDataComponentType);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class EntityRemoved extends HolderSystem<EntityStore> {
      private final ComponentType<EntityStore, CombatViewSystems.CombatData> combatDataComponentType;
      private final ResourceType<EntityStore, CombatViewSystems.CombatDataPool> dataPoolResourceType;

      public EntityRemoved(
         ComponentType<EntityStore, CombatViewSystems.CombatData> combatDataComponentType,
         ResourceType<EntityStore, CombatViewSystems.CombatDataPool> dataPoolResourceType
      ) {
         this.combatDataComponentType = combatDataComponentType;
         this.dataPoolResourceType = dataPoolResourceType;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.combatDataComponentType;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
         CombatViewSystems.CombatData combatData = holder.getComponent(this.combatDataComponentType);
         CombatViewSystems.CombatDataPool dataPool = store.getResource(this.dataPoolResourceType);
         CombatViewSystems.clearCombatData(combatData, dataPool);
      }
   }

   public static class Ticking extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, CombatViewSystems.CombatData> combatDataComponentType;
      private final ResourceType<EntityStore, CombatViewSystems.CombatDataPool> dataPoolResourceType;

      public Ticking(
         ComponentType<EntityStore, CombatViewSystems.CombatData> combatDataComponentType,
         ResourceType<EntityStore, CombatViewSystems.CombatDataPool> dataPoolResourceType
      ) {
         this.combatDataComponentType = combatDataComponentType;
         this.dataPoolResourceType = dataPoolResourceType;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.combatDataComponentType;
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return false;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         CombatViewSystems.CombatData combatData = archetypeChunk.getComponent(index, this.combatDataComponentType);
         CombatViewSystems.CombatDataPool dataPool = store.getResource(this.dataPoolResourceType);
         CombatViewSystems.clearCombatData(combatData, dataPool);
      }
   }
}
