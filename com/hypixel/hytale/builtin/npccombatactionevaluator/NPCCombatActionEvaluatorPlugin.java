package com.hypixel.hytale.builtin.npccombatactionevaluator;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.npccombatactionevaluator.conditions.RecentSustainedDamageCondition;
import com.hypixel.hytale.builtin.npccombatactionevaluator.conditions.TargetMemoryCountCondition;
import com.hypixel.hytale.builtin.npccombatactionevaluator.conditions.TotalSustainedDamageCondition;
import com.hypixel.hytale.builtin.npccombatactionevaluator.config.CombatBalanceAsset;
import com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.builders.BuilderActionAddToTargetMemory;
import com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.builders.BuilderActionCombatAbility;
import com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.builders.BuilderCombatTargetCollector;
import com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.builders.BuilderSensorCombatActionEvaluator;
import com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.builders.BuilderSensorHasHostileTargetMemory;
import com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.CombatActionEvaluator;
import com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.combatactions.CombatActionOption;
import com.hypixel.hytale.builtin.npccombatactionevaluator.memory.DamageMemory;
import com.hypixel.hytale.builtin.npccombatactionevaluator.memory.DamageMemorySystems;
import com.hypixel.hytale.builtin.npccombatactionevaluator.memory.TargetMemory;
import com.hypixel.hytale.builtin.npccombatactionevaluator.memory.TargetMemorySystems;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.config.balancing.BalanceAsset;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.Condition;
import javax.annotation.Nonnull;

public class NPCCombatActionEvaluatorPlugin extends JavaPlugin {
   @Nonnull
   public static final String CAE_MARKED_TARGET_SLOT = "CAETargetSlot";
   @Nonnull
   public static final String CAE_MIN_RANGE_PARAMETER = "CAEMinRange";
   @Nonnull
   public static final String CAE_MAX_RANGE_PARAMETER = "CAEMaxRange";
   @Nonnull
   public static final String CAE_POSITIONING_ANGLE_PARAMETER = "CAEPositioningAngle";
   private static NPCCombatActionEvaluatorPlugin instance;
   private ComponentType<EntityStore, TargetMemory> targetMemoryComponentType;
   private ComponentType<EntityStore, CombatActionEvaluator> combatActionEvaluatorComponentType;
   private ComponentType<EntityStore, CombatActionEvaluatorSystems.CombatConstructionData> combatConstructionDataComponentType;
   private ComponentType<EntityStore, DamageMemory> damageMemoryComponentType;

   public static NPCCombatActionEvaluatorPlugin get() {
      return instance;
   }

   public NPCCombatActionEvaluatorPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      instance = this;
      BalanceAsset.CODEC.register("CombatActionEvaluator", CombatBalanceAsset.class, CombatBalanceAsset.CODEC);
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                                 CombatActionOption.class, new IndexedLookupTableAssetMap<>(CombatActionOption[]::new)
                              )
                              .setPath("NPC/DecisionMaking/CombatActions"))
                           .setCodec(CombatActionOption.CODEC))
                        .setKeyFunction(CombatActionOption::getId))
                     .setReplaceOnRemove(CombatActionOption::getNothingFor))
                  .loadsAfter(Item.class, Condition.class, RootInteraction.class))
               .loadsBefore(BalanceAsset.class))
            .build()
      );
      NPCPlugin.get()
         .registerCoreComponentType("CombatActionEvaluator", BuilderSensorCombatActionEvaluator::new)
         .registerCoreComponentType("CombatTargets", BuilderCombatTargetCollector::new)
         .registerCoreComponentType("HasHostileTargetMemory", BuilderSensorHasHostileTargetMemory::new)
         .registerCoreComponentType("CombatAbility", BuilderActionCombatAbility::new)
         .registerCoreComponentType("AddToHostileTargetMemory", BuilderActionAddToTargetMemory::new);
      this.targetMemoryComponentType = this.getEntityStoreRegistry().registerComponent(TargetMemory.class, () -> {
         throw new UnsupportedOperationException("Not implemented");
      });
      this.combatActionEvaluatorComponentType = this.getEntityStoreRegistry().registerComponent(CombatActionEvaluator.class, () -> {
         throw new UnsupportedOperationException("Not implemented");
      });
      this.combatConstructionDataComponentType = this.getEntityStoreRegistry()
         .registerComponent(CombatActionEvaluatorSystems.CombatConstructionData.class, CombatActionEvaluatorSystems.CombatConstructionData::new);
      this.damageMemoryComponentType = this.getEntityStoreRegistry().registerComponent(DamageMemory.class, DamageMemory::new);
      this.getEntityStoreRegistry().registerSystem(new TargetMemorySystems.Ticking(this.targetMemoryComponentType));
      this.getEntityStoreRegistry()
         .registerSystem(
            new CombatActionEvaluatorSystems.EvaluatorTick(
               this.combatActionEvaluatorComponentType, this.targetMemoryComponentType, this.damageMemoryComponentType
            )
         );
      this.getEntityStoreRegistry().registerSystem(new DamageMemorySystems.CollectDamage(this.damageMemoryComponentType));
      this.getEntityStoreRegistry().registerSystem(new CombatActionEvaluatorSystems.OnAdded(this.combatConstructionDataComponentType));
      Condition.CODEC.register("RecentSustainedDamage", RecentSustainedDamageCondition.class, RecentSustainedDamageCondition.CODEC);
      Condition.CODEC.register("TotalSustainedDamage", TotalSustainedDamageCondition.class, TotalSustainedDamageCondition.CODEC);
      Condition.CODEC.register("KnownTargetCount", TargetMemoryCountCondition.class, TargetMemoryCountCondition.CODEC);
   }

   public ComponentType<EntityStore, TargetMemory> getTargetMemoryComponentType() {
      return this.targetMemoryComponentType;
   }

   public ComponentType<EntityStore, CombatActionEvaluator> getCombatActionEvaluatorComponentType() {
      return this.combatActionEvaluatorComponentType;
   }

   public ComponentType<EntityStore, CombatActionEvaluatorSystems.CombatConstructionData> getCombatConstructionDataComponentType() {
      return this.combatConstructionDataComponentType;
   }

   public ComponentType<EntityStore, DamageMemory> getDamageMemoryComponentType() {
      return this.damageMemoryComponentType;
   }
}
