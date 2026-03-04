package com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.combatactions;

import com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.CombatActionEvaluator;
import com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.CombatActionEvaluatorConfig;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.Option;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import com.hypixel.hytale.server.npc.valuestore.ValueStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class BasicAttackTargetCombatAction extends CombatActionOption {
   private static final double BASIC_ATTACK_DISTANCE_OFFSET = 0.1;
   @Nonnull
   public static final BuilderCodec<BasicAttackTargetCombatAction> CODEC = BuilderCodec.builder(
         BasicAttackTargetCombatAction.class, BasicAttackTargetCombatAction::new, Option.ABSTRACT_CODEC
      )
      .documentation("A combat action which simply selects a target and sets up distances for use with substates that only contain basic attacks.")
      .<Integer>appendInherited(
         new KeyedCodec<>("WeaponSlot", Codec.INTEGER),
         (option, i) -> option.weaponSlot = i,
         option -> option.weaponSlot,
         (option, parent) -> option.weaponSlot = parent.weaponSlot
      )
      .documentation("The weapon (hotbar) slot to switch to for basic attacks.")
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("OffhandSlot", Codec.INTEGER),
         (option, i) -> option.offhandSlot = i,
         option -> option.offhandSlot,
         (option, parent) -> option.offhandSlot = parent.offhandSlot
      )
      .documentation("The off-hand slot to switch to for basic attacks. -1 set to no off-hand equipped.")
      .add()
      .afterDecode(option -> option.actionTarget = CombatActionOption.Target.Hostile)
      .build();
   protected int weaponSlot;
   protected int offhandSlot;

   @Override
   public void execute(
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      CommandBuffer<EntityStore> commandBuffer,
      Role role,
      @Nonnull CombatActionEvaluator evaluator,
      @Nonnull ValueStore valueStore
   ) {
      NPCEntity npcComponent = archetypeChunk.getComponent(index, NPCEntity.getComponentType());

      assert npcComponent != null;

      HytaleLogger.Api ctx = CombatActionEvaluator.LOGGER.at(Level.FINEST);
      if (ctx.isEnabled()) {
         ctx.log("%s: Executing option %s", archetypeChunk.getReferenceTo(index), this.getId());
      }

      InventoryHelper.setHotbarSlot(npcComponent.getInventory(), (byte)this.weaponSlot);
      InventoryHelper.setOffHandSlot(npcComponent.getInventory(), (byte)this.offhandSlot);
      CombatActionEvaluatorConfig.BasicAttacks basicAttacks = evaluator.getCurrentBasicAttackSet();
      if (basicAttacks != null) {
         double range = basicAttacks.getMaxRange() - 0.1;
         valueStore.storeDouble(evaluator.getMinRangeSlot(), range);
         valueStore.storeDouble(evaluator.getMaxRangeSlot(), range);
      }

      evaluator.completeCurrentAction(true, false);
      evaluator.clearTimeout();
   }

   @Override
   public boolean isBasicAttackAllowed(
      int selfIndex, ArchetypeChunk<EntityStore> archetypeChunk, CommandBuffer<EntityStore> commandBuffer, CombatActionEvaluator evaluator
   ) {
      return true;
   }

   @Override
   public boolean cancelBasicAttackOnSelect() {
      return false;
   }
}
