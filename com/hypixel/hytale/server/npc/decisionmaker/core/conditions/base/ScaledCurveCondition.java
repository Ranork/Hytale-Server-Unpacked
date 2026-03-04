package com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.responsecurve.ScaledResponseCurve;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import javax.annotation.Nonnull;

public abstract class ScaledCurveCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<ScaledCurveCondition> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(ScaledCurveCondition.class, BASE_CODEC)
      .appendInherited(
         new KeyedCodec<>("Curve", ScaledResponseCurve.CODEC),
         (condition, s) -> condition.responseCurve = s,
         condition -> condition.responseCurve,
         (condition, parent) -> condition.responseCurve = parent.responseCurve
      )
      .documentation("The scaled response curve used to evaluate the condition.")
      .addValidator(Validators.nonNull())
      .add()
      .build();
   protected ScaledResponseCurve responseCurve;

   protected ScaledCurveCondition() {
   }

   public ScaledResponseCurve getResponseCurve() {
      return this.responseCurve;
   }

   @Override
   public double calculateUtility(
      int selfIndex, ArchetypeChunk<EntityStore> archetypeChunk, Ref<EntityStore> target, CommandBuffer<EntityStore> commandBuffer, EvaluationContext context
   ) {
      double input = this.getInput(selfIndex, archetypeChunk, target, commandBuffer, context);
      return input == Double.MAX_VALUE ? 0.0 : this.responseCurve.computeY(input);
   }

   @Override
   public int getSimplicity() {
      return 30;
   }

   protected abstract double getInput(
      int var1, ArchetypeChunk<EntityStore> var2, Ref<EntityStore> var3, CommandBuffer<EntityStore> var4, EvaluationContext var5
   );

   @Nonnull
   @Override
   public String toString() {
      return "ScaledCurveCondition{responseCurve=" + this.responseCurve + "} " + super.toString();
   }
}
