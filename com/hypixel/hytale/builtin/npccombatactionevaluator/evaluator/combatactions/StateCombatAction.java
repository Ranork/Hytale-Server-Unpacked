package com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.combatactions;

import com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.CombatActionEvaluator;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.valuestore.ValueStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class StateCombatAction extends CombatActionOption {
   @Nonnull
   public static final BuilderCodec<StateCombatAction> CODEC = BuilderCodec.builder(
         StateCombatAction.class, StateCombatAction::new, CombatActionOption.BASE_CODEC
      )
      .documentation(
         "A combat action which switches the NPCs state. Using substate only will switch between combat substates, whereas including the main state can be used to transition out of combat."
      )
      .<String>append(new KeyedCodec<>("State", Codec.STRING), (option, s) -> option.state = s, option -> option.state)
      .documentation("The main state name.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyString())
      .add()
      .<String>append(new KeyedCodec<>("SubState", Codec.STRING), (option, s) -> option.subState = s, option -> option.subState)
      .documentation("The substate name.")
      .add()
      .build();
   protected String state;
   protected String subState;

   public String getState() {
      return this.state;
   }

   public String getSubState() {
      return this.subState;
   }

   @Override
   public void execute(
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Role role,
      @Nonnull CombatActionEvaluator evaluator,
      ValueStore valueStore
   ) {
      Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
      role.getStateSupport().setState(ref, this.state, this.subState, commandBuffer);
      evaluator.completeCurrentAction(true, true);
      evaluator.clearTimeout();
      HytaleLogger.Api ctx = CombatActionEvaluator.LOGGER.at(Level.FINEST);
      if (ctx.isEnabled()) {
         ctx.log("%s: Set state to %s.%s", archetypeChunk.getReferenceTo(index), this.state, this.subState == null ? "Default" : this.subState);
      }
   }

   @Override
   public boolean isBasicAttackAllowed(
      int selfIndex, ArchetypeChunk<EntityStore> archetypeChunk, CommandBuffer<EntityStore> commandBuffer, CombatActionEvaluator evaluator
   ) {
      return false;
   }

   @Nonnull
   @Override
   public String toString() {
      return "StateCombatAction{state='" + this.state + "', subState='" + this.subState + "'}" + super.toString();
   }
}
