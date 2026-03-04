package com.hypixel.hytale.server.npc.decisionmaker.stateevaluator;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.StateMappingHelper;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.Evaluator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class StateEvaluator extends Evaluator<StateOption> implements Component<EntityStore> {
   public static final BuilderCodec<StateEvaluator> CODEC = BuilderCodec.builder(StateEvaluator.class, StateEvaluator::new)
      .append(
         new KeyedCodec<>("Options", new ArrayCodec<>(StateOption.CODEC, StateOption[]::new)),
         (evaluator, o) -> evaluator.rawOptions = o,
         evaluator -> evaluator.rawOptions
      )
      .documentation("The list of state options to evaluate.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyArray())
      .add()
      .<Double>append(
         new KeyedCodec<>("ExecutionFrequency", Codec.DOUBLE), (evaluator, d) -> evaluator.executeFrequency = d, evaluator -> evaluator.executeFrequency
      )
      .documentation("The frequency with which the state evaluator should be run.")
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<Double>append(
         new KeyedCodec<>("StateChangeCooldown", Codec.DOUBLE), (evaluator, d) -> evaluator.stateChangeCooldown = d, evaluator -> evaluator.stateChangeCooldown
      )
      .documentation("The delay before performing the next state evaluation after a successful switch to another state.")
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<Double>append(
         new KeyedCodec<>("MinimumConsideredUtility", Codec.DOUBLE), (evaluator, d) -> evaluator.minimumUtility = d, evaluator -> evaluator.minimumUtility
      )
      .documentation("The minimum utility value to consider when selecting from evaluated options.")
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .build();
   protected StateOption[] rawOptions;
   protected double executeFrequency = 0.1;
   protected double stateChangeCooldown = 2.0;
   protected double minimumUtility = 0.1;
   private double timeUntilNextExecute;
   private boolean active = true;
   private final EvaluationContext evaluationContext = new EvaluationContext();

   public static ComponentType<EntityStore, StateEvaluator> getComponentType() {
      return NPCPlugin.get().getStateEvaluatorComponentType();
   }

   protected StateEvaluator() {
   }

   public boolean isActive() {
      return this.active;
   }

   public void setActive(boolean active) {
      this.active = active;
   }

   @Nonnull
   public EvaluationContext getEvaluationContext() {
      return this.evaluationContext;
   }

   public void prepareOptions(@Nonnull StateMappingHelper stateHelper) {
      if (this.options == null) {
         this.options = new ObjectArrayList(this.rawOptions.length);

         for (StateOption option : this.rawOptions) {
            stateHelper.getAndPutSetterIndex(option.getState(), option.getSubState(), option::setStateIndex);
            this.options.add(new StateEvaluator.SelfOptionHolder(option));
         }
      }
   }

   public boolean shouldExecute(double interval) {
      if ((this.timeUntilNextExecute -= interval) > 0.0) {
         return false;
      } else {
         this.timeUntilNextExecute = this.executeFrequency;
         return true;
      }
   }

   public void prepareEvaluationContext(@Nonnull EvaluationContext context) {
      context.setMinimumUtility(this.minimumUtility);
      context.setMinimumWeightCoefficient(0.0);
      context.setPredictability(1.0F);
   }

   public void onStateSwitched() {
      this.timeUntilNextExecute = this.stateChangeCooldown;
   }

   @Nonnull
   @Override
   public String toString() {
      return "StateEvaluator{executeFrequency="
         + this.executeFrequency
         + ", stateChangeCooldown="
         + this.stateChangeCooldown
         + ", minimumUtility="
         + this.minimumUtility
         + ", rawOptions="
         + Arrays.toString((Object[])this.rawOptions)
         + "}";
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      StateEvaluator evaluator = new StateEvaluator();
      evaluator.options = this.options;
      evaluator.rawOptions = this.rawOptions;
      evaluator.executeFrequency = this.executeFrequency;
      evaluator.stateChangeCooldown = this.stateChangeCooldown;
      evaluator.minimumUtility = this.minimumUtility;
      evaluator.timeUntilNextExecute = this.timeUntilNextExecute;
      evaluator.active = this.active;
      return evaluator;
   }

   public class SelfOptionHolder extends Evaluator<StateOption>.OptionHolder {
      public SelfOptionHolder(StateOption option) {
         super(option);
      }

      @Override
      public double calculateUtility(
         int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, CommandBuffer<EntityStore> commandBuffer, @Nonnull EvaluationContext context
      ) {
         return this.utility = ((StateOption)this.option).calculateUtility(index, archetypeChunk, archetypeChunk.getReferenceTo(index), commandBuffer, context);
      }
   }
}
