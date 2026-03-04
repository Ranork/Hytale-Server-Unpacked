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
import javax.annotation.Nonnull;

public class HasTargetCondition extends SimpleCondition {
   public static final BuilderCodec<HasTargetCondition> CODEC = BuilderCodec.builder(HasTargetCondition.class, HasTargetCondition::new, ABSTRACT_CODEC)
      .documentation("A simple boolean condition that returns whether the NPC has a target locked in the given slot.")
      .<String>appendInherited(
         new KeyedCodec<>("TargetSlot", Codec.STRING),
         (condition, s) -> condition.targetSlot = s,
         condition -> condition.targetSlot,
         (condition, parent) -> condition.targetSlot = parent.targetSlot
      )
      .documentation("The target slot to check.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyString())
      .add()
      .build();
   protected String targetSlot;

   protected HasTargetCondition() {
   }

   public String getTargetSlot() {
      return this.targetSlot;
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

      return npcComponent.getRole().getMarkedEntitySupport().hasMarkedEntityInSlot(this.targetSlot);
   }

   @Nonnull
   @Override
   public String toString() {
      return "HasTargetCondition{targetSlot=" + this.targetSlot + "} " + super.toString();
   }
}
