package com.hypixel.hytale.server.npc.decisionmaker.core;

import com.hypixel.hytale.common.map.IWeightedElement;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Evaluator<OptionType extends Option> {
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static long NOT_USED = 0L;
   protected List<Evaluator<OptionType>.OptionHolder> options;

   public void initialise() {
      this.options.sort(Comparator.comparingDouble(Evaluator.OptionHolder::getWeightCoefficient).reversed());

      for (Evaluator<OptionType>.OptionHolder optionHolder : this.options) {
         optionHolder.option.sortConditions();
      }
   }

   public void setupNPC(Role role) {
      for (Evaluator<OptionType>.OptionHolder optionHolder : this.options) {
         optionHolder.option.setupNPC(role);
      }
   }

   public void setupNPC(Holder<EntityStore> holder) {
      for (Evaluator<OptionType>.OptionHolder optionHolder : this.options) {
         optionHolder.option.setupNPC(holder);
      }
   }

   @Nullable
   public Evaluator<OptionType>.OptionHolder evaluate(
      int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, CommandBuffer<EntityStore> commandBuffer, @Nonnull EvaluationContext context
   ) {
      NPCEntity npcComponent = archetypeChunk.getComponent(index, NPCEntity.getComponentType());

      assert npcComponent != null;

      UUIDComponent uuidComponent = archetypeChunk.getComponent(index, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      Evaluator<OptionType>.OptionHolder bestOption = null;
      double minimumWeight = context.getMinimumWeightCoefficient();
      int nonMatchingIndex = this.options.size();

      for (int i = 0; i < this.options.size(); i++) {
         Evaluator<OptionType>.OptionHolder optionHolder = this.options.get(i);
         if (optionHolder.getWeightCoefficient() < minimumWeight) {
            nonMatchingIndex = i;
            break;
         }

         double utility = optionHolder.calculateUtility(index, archetypeChunk, commandBuffer, context);
         HytaleLogger.Api logContext = LOGGER.at(Level.FINE);
         if (logContext.isEnabled()) {
            logContext.log("%s with uuid %s: Scored option %s at %s", npcComponent.getRoleName(), uuidComponent.getUuid(), optionHolder.option, utility);
         }

         if (!(utility <= 0.0) && (bestOption == null || utility > bestOption.utility)) {
            bestOption = optionHolder;
         }
      }

      if (bestOption == null) {
         return null;
      } else {
         float predictability = context.getPredictability();
         if (predictability == 1.0F) {
            return bestOption;
         } else {
            double threshold = bestOption.utility * predictability;
            double sum = 0.0;

            for (int i = 0; i < nonMatchingIndex; i++) {
               Evaluator<OptionType>.OptionHolder optionHolderx = this.options.get(i);
               if (optionHolderx.utility >= threshold) {
                  sum += optionHolderx.getTotalUtility(threshold);
               }
            }

            double randomWeight = ThreadLocalRandom.current().nextDouble(sum);

            for (int ix = 0; ix < nonMatchingIndex; ix++) {
               Evaluator<OptionType>.OptionHolder optionHolderx = this.options.get(ix);
               if (!(optionHolderx.utility < threshold)) {
                  randomWeight = optionHolderx.tryPick(randomWeight, threshold);
                  if (randomWeight <= 0.0) {
                     bestOption = optionHolderx;
                     break;
                  }
               }
            }

            return bestOption;
         }
      }
   }

   public abstract class OptionHolder implements IWeightedElement {
      protected final OptionType option;
      protected double utility;

      public OptionHolder(OptionType option) {
         this.option = option;
      }

      @Override
      public double getWeight() {
         return this.utility;
      }

      public double getWeightCoefficient() {
         return this.option.getWeightCoefficient();
      }

      public OptionType getOption() {
         return this.option;
      }

      public double getTotalUtility(double threshold) {
         return this.utility;
      }

      public double tryPick(double currentWeight, double threshold) {
         return currentWeight - this.utility;
      }

      public abstract double calculateUtility(int var1, ArchetypeChunk<EntityStore> var2, CommandBuffer<EntityStore> var3, EvaluationContext var4);
   }
}
