package com.hypixel.hytale.server.npc.decisionmaker.core.conditions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.SimpleCondition;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import javax.annotation.Nonnull;

public class IsInStateCondition extends SimpleCondition {
   public static final BuilderCodec<IsInStateCondition> CODEC = BuilderCodec.builder(IsInStateCondition.class, IsInStateCondition::new, ABSTRACT_CODEC)
      .documentation("A simple boolean condition that returns whether the NPC is in a given state.")
      .<String>appendInherited(
         new KeyedCodec<>("State", Codec.STRING),
         (condition, s) -> condition.state = s,
         condition -> condition.state,
         (condition, parent) -> condition.state = parent.state
      )
      .documentation("The main state to evaluate.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyString())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("SubState", Codec.STRING),
         (condition, s) -> condition.subState = s,
         condition -> condition.subState,
         (condition, parent) -> condition.subState = parent.subState
      )
      .documentation("The optional substate to evaluate.")
      .add()
      .build();
   protected String state;
   protected String subState;

   protected IsInStateCondition() {
   }

   public String getState() {
      return this.state;
   }

   @Override
   protected boolean evaluate(
      int selfIndex,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      Ref<EntityStore> target,
      CommandBuffer<EntityStore> commandBuffer,
      EvaluationContext context
   ) {
      NPCEntity npcComponent = archetypeChunk.getComponent(selfIndex, NPCEntity.getComponentType());

      assert npcComponent != null;

      StateSupport stateSupport = npcComponent.getRole().getStateSupport();
      return stateSupport.inState(this.state, this.subState);
   }

   @Nonnull
   @Override
   public String toString() {
      return "IsInStateCondition{state=" + this.state + ", subState=" + this.subState + "} " + super.toString();
   }
}
