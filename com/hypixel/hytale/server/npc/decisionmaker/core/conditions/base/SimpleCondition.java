package com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import javax.annotation.Nonnull;

public abstract class SimpleCondition extends Condition {
   public static final BuilderCodec<SimpleCondition> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(SimpleCondition.class, BASE_CODEC)
      .appendInherited(
         new KeyedCodec<>("FalseValue", Codec.DOUBLE),
         (condition, d) -> condition.falseValue = d,
         condition -> condition.falseValue,
         (condition, parent) -> condition.falseValue = parent.falseValue
      )
      .documentation("The utility value to use when the condition evaluates false.")
      .addValidator(Validators.range(0.0, 1.0))
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("TrueValue", Codec.DOUBLE),
         (condition, d) -> condition.trueValue = d,
         condition -> condition.trueValue,
         (condition, parent) -> condition.trueValue = parent.trueValue
      )
      .documentation("The utility value to use when the condition evaluates true.")
      .addValidator(Validators.range(0.0, 1.0))
      .add()
      .build();
   protected double falseValue = 0.0;
   protected double trueValue = 1.0;

   protected SimpleCondition() {
   }

   @Override
   public double calculateUtility(
      int selfIndex, ArchetypeChunk<EntityStore> archetypeChunk, Ref<EntityStore> target, CommandBuffer<EntityStore> commandBuffer, EvaluationContext context
   ) {
      return this.evaluate(selfIndex, archetypeChunk, target, commandBuffer, context) ? this.trueValue : this.falseValue;
   }

   @Override
   public int getSimplicity() {
      return 10;
   }

   protected abstract boolean evaluate(
      int var1, ArchetypeChunk<EntityStore> var2, Ref<EntityStore> var3, CommandBuffer<EntityStore> var4, EvaluationContext var5
   );

   @Nonnull
   @Override
   public String toString() {
      return "SimpleCondition{falseValue=" + this.falseValue + ", trueValue=" + this.trueValue + "} " + super.toString();
   }
}
