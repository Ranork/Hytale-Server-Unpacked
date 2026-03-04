package com.hypixel.hytale.server.npc.decisionmaker.core;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.Condition;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public abstract class Option {
   public static final BuilderCodec<Option> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(Option.class)
      .append(new KeyedCodec<>("Description", Codec.STRING), (option, s) -> option.description = s, option -> option.description)
      .documentation("A friendly description of this option's outcome.")
      .add()
      .<String[]>appendInherited(
         new KeyedCodec<>("Conditions", Condition.CHILD_ASSET_CODEC_ARRAY),
         (option, s) -> option.conditions = s,
         option -> option.conditions,
         (option, parent) -> option.conditions = parent.conditions
      )
      .documentation("The list of conditions for evaluating this option's utility.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyArray())
      .addValidator(Condition.VALIDATOR_CACHE.getArrayValidator())
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("WeightCoefficient", Codec.DOUBLE),
         (option, d) -> option.weightCoefficient = d,
         option -> option.weightCoefficient,
         (option, parent) -> option.weightCoefficient = parent.weightCoefficient
      )
      .documentation("An additional weighted ranking that can be used to greatly increase the utility of this option.")
      .addValidator(Validators.greaterThanOrEqual(1.0))
      .add()
      .build();
   protected String description;
   protected String[] conditions;
   protected double weightCoefficient = 1.0;
   protected Option.ConditionReference[] sortedConditions;

   protected Option() {
   }

   public String[] getConditions() {
      return this.conditions;
   }

   public double getWeightCoefficient() {
      return this.weightCoefficient;
   }

   public void sortConditions() {
      this.sortedConditions = new Option.ConditionReference[this.conditions.length];

      for (int i = 0; i < this.conditions.length; i++) {
         Condition condition = Condition.getAssetMap().getAsset(this.conditions[i]);
         if (condition == null) {
            throw new IllegalStateException("Condition '" + this.conditions[i] + "' does not exist!");
         }

         this.sortedConditions[i] = new Option.ConditionReference(Condition.getAssetMap().getIndex(this.conditions[i]), condition);
      }

      Arrays.sort(this.sortedConditions, Comparator.comparingInt(Option.ConditionReference::getSimplicity));
   }

   public void setupNPC(Role role) {
      for (Option.ConditionReference condition : this.sortedConditions) {
         condition.get().setupNPC(role);
      }
   }

   public void setupNPC(Holder<EntityStore> holder) {
      for (Option.ConditionReference condition : this.sortedConditions) {
         condition.get().setupNPC(holder);
      }
   }

   public double calculateUtility(
      int selfIndex,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      Ref<EntityStore> target,
      CommandBuffer<EntityStore> commandBuffer,
      @Nonnull EvaluationContext context
   ) {
      NPCEntity npcComponent = archetypeChunk.getComponent(selfIndex, NPCEntity.getComponentType());

      assert npcComponent != null;

      UUIDComponent uuidComponent = archetypeChunk.getComponent(selfIndex, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      double compensationFactor = 1.0 - 1.0 / this.sortedConditions.length;
      double result = 1.0;

      for (Option.ConditionReference reference : this.sortedConditions) {
         Condition condition = reference.get();
         double score = condition.calculateUtility(selfIndex, archetypeChunk, target, commandBuffer, context);
         HytaleLogger.Api logContext = Evaluator.LOGGER.at(Level.FINE);
         if (logContext.isEnabled()) {
            logContext.log("%s with uuid %s: Scored condition %s at %s", npcComponent.getRoleName(), uuidComponent.getUuid(), condition, score);
         }

         result *= score + (1.0 - score) * compensationFactor * score;
         if (result == 0.0 || result < context.getMinimumUtility()) {
            return 0.0;
         }
      }

      return result * this.weightCoefficient;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Option{description="
         + this.description
         + ", conditions="
         + Arrays.toString((Object[])this.conditions)
         + ", sortedConditions="
         + Arrays.toString((Object[])this.sortedConditions)
         + ", weightCoefficient="
         + this.weightCoefficient
         + "}";
   }

   private static class ConditionReference {
      private final int index;
      private WeakReference<Condition> reference;

      private ConditionReference(int index, @Nonnull Condition condition) {
         this.index = index;
         this.reference = condition.getReference();
      }

      @Nonnull
      private Condition get() {
         Condition condition = this.reference.get();
         if (condition == null) {
            condition = Condition.getAssetMap().getAsset(this.index);
            this.reference = condition.getReference();
         }

         return condition;
      }

      private int getSimplicity() {
         return this.get().getSimplicity();
      }
   }
}
